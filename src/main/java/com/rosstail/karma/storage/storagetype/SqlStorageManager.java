package com.rosstail.karma.storage.storagetype;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.players.PlayerDataManager;
import com.rosstail.karma.players.PlayerModel;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlStorageManager implements IStorageManager {
    protected final Karma plugin = Karma.getInstance();
    protected final String pluginName;
    protected String driver;
    protected String url;
    protected String username;
    protected String password;
    protected Connection connection;

    public SqlStorageManager(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        createKarmaTable();
    }

    public void createKarmaTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + pluginName + " ( uuid varchar(40) PRIMARY KEY UNIQUE NOT NULL," +
                " karma float NOT NULL DEFAULT 0," +
                " previous_karma float NOT NULL DEFAULT 0," +
                " tier varchar(50)," +
                " previous_tier varchar(50)," +
                " wanted_time bigint UNSIGNED NOT NULL  DEFAULT 0," +
                " is_wanted boolean NOT NULL DEFAULT false," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP) CHARACTER SET utf8 COLLATE utf8_unicode_ci;";
        executeSQL(query);
    }

    @Override
    public boolean insertPlayerModel(PlayerModel model) {
        String query = "INSERT INTO " + pluginName + " (uuid, karma, previous_karma, tier, previous_tier)"
                + " VALUES (?, ?, ?, ?, ?);";

        String uuid = model.getUuid();
        float karma = model.getKarma();
        float previousKarma = model.getPreviousKarma();
        String tierName = model.getTierName();
        String previousTierName = model.getPreviousTierName();
        try {
            return executeSQLUpdate(query, uuid, karma, previousKarma, tierName, previousTierName) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PlayerModel selectPlayerModel(String uuid) {
        String query = "SELECT * FROM " + pluginName + " WHERE uuid = ?";
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, uuid);
            if (result.next()) {
                PlayerModel model = new PlayerModel(uuid, PlayerDataManager.getPlayerNameFromUUID(uuid));
                model.setKarma(result.getFloat("karma"));
                model.setPreviousKarma(result.getFloat("previous_karma"));
                model.setTierName(result.getString("tier"));
                model.setPreviousTierName(result.getString("previous_tier"));

                long lastUpdateUTC = result.getTimestamp("last_update").getTime();
                model.setLastUpdate(lastUpdateUTC);

                long wantedTime = result.getLong("wanted_time");
                if (ConfigData.getConfigData().wanted.wantedCountdownApplyOnDisconnect) {
                    model.setWantedTimeStamp(new Timestamp(model.getLastUpdate() + wantedTime));
                } else {
                    model.setWantedTimeStamp(new Timestamp(System.currentTimeMillis() + wantedTime));
                }
                model.setWanted(result.getBoolean("is_wanted"));
                return model;
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePlayerModelAsync(PlayerModel model) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                updatePlayerModel(model);
            }
        });
    }

    @Override
    public void updatePlayerModel(PlayerModel model) {
        String query = "UPDATE " + pluginName + " SET karma = ?, previous_karma = ?, tier = ?, previous_tier = ?, wanted_time = ?, is_wanted = ?, last_update = CURRENT_TIMESTAMP WHERE uuid = ?";

        try {
            boolean success = executeSQLUpdate(query,
                    model.getKarma(), model.getPreviousKarma(),
                    model.getTierName(), model.getPreviousTierName(),
                    PlayerDataManager.getWantedTimeLeft(model),
                    model.isWanted(),
                    model.getUuid())
                    > 0;

            if (success) {
                model.setLastUpdate(System.currentTimeMillis());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePlayerModel(String uuid) {
        String query = "DELETE FROM " + pluginName + " WHERE uuid = ?";
        try {
            boolean success = executeSQLUpdate(query, uuid) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes an SQL request for INSERT, UPDATE and DELETE
     *
     * @param query # The query itself
     * @param params #The values to put as WHERE
     * @return # Returns the number of rows affected
     */
    private int executeSQLUpdate(String query, Object... params) throws SQLException {
        int result = 0;
        try (PreparedStatement statement = openConnection().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            result = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Executes an SQL request for SELECT
     *
     * @param query # The query itself
     * @param params #The values to put as WHERE
     * @return # Returns the ResultSet of the request
     */
    public ResultSet executeSQLQuery(Connection connection, String query, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Executes an SQL request to CREATE TABLE
     *
     * @param query # The query itself
     * @return # Returns if the request succeeded
     */
    public boolean executeSQL(String query, Object... params) {
        boolean execute = false;
        try {
            PreparedStatement statement = openConnection().prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            execute = statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return execute;
    }

    public Connection openConnection() {
        try {
            if (connection != null && !connection.isClosed() && connection.isValid(1)) {
                return connection;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            if (driver != null) {
                Class.forName(driver);
            }

            if (username != null) {
                connection = DriverManager.getConnection(url, username, password);
            } else {
                connection = DriverManager.getConnection(url);
            }

            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<PlayerModel> selectPlayerModelListAsc(int limit) {
        List<String> onlineUuidList = new ArrayList<>();
        PlayerDataManager.getPlayerModelMap().forEach((s, playerModel) -> {
            onlineUuidList.add(playerModel.getUuid());
        });

        String query = "SELECT * FROM " + pluginName;
        if (onlineUuidList.size() > 0) {
            StringBuilder replacement = new StringBuilder("(");
            for (int i = 0; i < onlineUuidList.size(); i++) {
                replacement.append("'").append(onlineUuidList.get(i)).append("'");
                if (i < onlineUuidList.size() - 1) {
                    replacement.append(",");
                }
            }
            replacement.append(")");
            query += " WHERE " + pluginName + ".uuid NOT IN " + replacement;
        }
        query += " ORDER BY " + pluginName + ".karma ASC LIMIT ?";
        return selectPlayerModelList(query, limit);
    }

    public List<PlayerModel> selectPlayerModelListDesc(int limit) {
        List<String> onlineUUIDList = new ArrayList<>();

        PlayerDataManager.getPlayerModelMap().forEach((s, playerModel) -> {
            onlineUUIDList.add(playerModel.getUuid());
        });

        String query = "SELECT * FROM " + pluginName;

        if (onlineUUIDList.size() > 0) {
            StringBuilder replacement = new StringBuilder("(");
            for (int i = 0; i < onlineUUIDList.size(); i++) {
                replacement.append("'").append(onlineUUIDList.get(i)).append("'");
                if (i < onlineUUIDList.size() - 1) {
                    replacement.append(",");
                }
            }
            replacement.append(")");
            query += " WHERE " + pluginName + ".uuid NOT IN " + replacement;
        }
        query += " ORDER BY " + pluginName + ".karma DESC LIMIT ?";
        return selectPlayerModelList(query, limit);
    }

    @Override
    public List<PlayerModel> selectPlayerModelList(String query, int limit) {
        List<PlayerModel> modelList = new ArrayList<>();
        try {
            ResultSet result = executeSQLQuery(openConnection(), query, limit);
            while (result.next()) {
                String uuid = result.getString("uuid");
                String username = PlayerDataManager.getPlayerNameFromUUID(uuid);
                PlayerModel model = new PlayerModel(uuid, username);
                model.setKarma(result.getFloat("karma"));
                model.setPreviousKarma(result.getFloat("previous_karma"));
                model.setTierName(result.getString("tier"));
                model.setPreviousTierName(result.getString("previous_tier"));
                model.setLastUpdate(result.getTimestamp("last_update").getTime());
                model.setWantedTimeStamp(new Timestamp(model.getLastUpdate() + result.getLong("wanted_time"))); //A modifier
                model.setWanted(model.getWantedTimeStamp().getTime() > System.currentTimeMillis());
                modelList.add(model);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modelList;
    }

}
