package com.rosstail.karma.datas.storage.storagetype;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MariaDBStorageRequest implements StorageRequest {
    private final String pluginName;
    private Connection sqlConnection;

    public MariaDBStorageRequest(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        try {
            // Connexion à la base de données SQL
            Class.forName("org.mariadb.jdbc.Driver");
            String url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
            sqlConnection = DriverManager.getConnection(url, username, password);
            createKarmaTable();
        } catch (Exception e) {
            e.printStackTrace();
            // Gérer les erreurs de connexion ici
        }
    }

    public void disconnect() {
        if (sqlConnection != null) {
            try {
                sqlConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createKarmaTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + pluginName + " ( uuid varchar(40) PRIMARY KEY UNIQUE NOT NULL," +
                " karma float NOT NULL DEFAULT 0," +
                " previous_karma float NOT NULL DEFAULT 0," +
                " tier varchar(50)," +
                " previous_tier varchar(50)," +
                " wanted_time bigint UNSIGNED NOT NULL  DEFAULT 0," +
                " is_wanted boolean NOT NULL DEFAULT false," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public boolean insertPayerModel(PlayerModel model) {
        String query = "INSERT INTO " + pluginName + " (uuid, karma, previous_karma, tier, previous_tier)"
                + " VALUES (?, ?, ?, ?, ?);";

        String uuid = model.getUuid();
        float karma = model.getKarma();
        float previousKarma = model.getPreviousKarma();
        String tierName = model.getTierName();
        String previousTierName = model.getPreviousTierName();
        try {
            boolean success = executeSQLUpdate(query, uuid, karma, previousKarma, tierName, previousTierName) > 0;
            System.out.println("INSERT SUCCESS " + success);
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PlayerModel selectPlayerModel(String uuid) {
        String query = "SELECT * FROM " + pluginName + " WHERE uuid = ?";
        try {
            ResultSet result = executeSQLQuery(query, uuid);
            if (result.next()) {
                System.out.println("Found.");
                PlayerModel model = new PlayerModel(uuid, PlayerDataManager.getPlayerNameFromUUID(uuid));
                model.setKarma(result.getFloat("karma"));
                model.setPreviousKarma(result.getFloat("previous_karma"));
                model.setTierName(result.getString("tier"));
                model.setPreviousTierName(result.getString("previous_tier"));
                model.setLastUpdate(result.getTimestamp("last_update").getTime());
                long wantedTime = result.getLong("wanted_time");
                if (ConfigData.getConfigData().wanted.wantedCountdownApplyOnDisconnect) {
                    model.setWantedTimeStamp(new Timestamp(model.getLastUpdate() + wantedTime));
                } else {
                    model.setWantedTimeStamp(new Timestamp(System.currentTimeMillis() + wantedTime));
                }
                model.setWanted(model.getWantedTimeStamp().getTime() > System.currentTimeMillis());
                return model;
            } else {
                System.out.println("Not found.");
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updatePlayerModel(PlayerModel model) {
        String query = "UPDATE " + pluginName + " SET karma = ?, previous_karma = ?, tier = ?, previous_tier = ?, wanted_time = ?, is_wanted = ?, last_update = CURRENT_TIMESTAMP WHERE uuid = ?";
        try {
            boolean success = executeSQLUpdate(query,
                    model.getKarma(), model.getPreviousKarma(),
                    model.getTierName(),model.getPreviousTierName(),
                    PlayerDataManager.getWantedTimeLeft(model),
                    model.isWanted(),
                    model.getUuid())
                    > 0;
            if (success) {
                System.out.println("Updated successfully");
            } else {
                System.out.println("Nope");
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
            if (success) {
                System.out.println("Deleted successfully");
            } else {
                System.out.println("does not exist");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PlayerModel> selectPlayerModelListAsc(int limit) {
        List<String> onlineUUIDList = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> {
            onlineUUIDList.add(player.getUniqueId().toString());
        });
        String[] onlineUuidArray = onlineUUIDList.toArray(new String[0]);

        if (onlineUuidArray.length > 0) {
            String query = "SELECT * FROM " + pluginName +
                    " WHERE " + pluginName + ".uuid NOT IN ?" +
                    " ORDER BY " + pluginName +  ".karma ASC LIMIT ?";
            return selectPlayerModelList(query, limit);
        }
        String query = "SELECT * FROM " + pluginName + " ORDER BY " + pluginName +  ".karma ASC LIMIT ?";
        return selectPlayerModelList(query, limit);
    }

    public List<PlayerModel> selectPlayerModelListDesc(int limit) {
        List<String> onlineUUIDList = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> {
            onlineUUIDList.add(player.getUniqueId().toString());
        });
        String[] onlineUuidArray = onlineUUIDList.toArray(new String[0]);

        if (onlineUuidArray.length > 0) {
            String query = "SELECT * FROM " + pluginName +
                    " WHERE " + pluginName + ".uuid NOT IN ?" +
                    " ORDER BY " + pluginName +  ".karma DESC LIMIT ?";
            return selectPlayerModelList(query, limit);
        }
        String query = "SELECT * FROM " + pluginName + " ORDER BY " + pluginName +  ".karma DESC LIMIT ?";
        return selectPlayerModelList(query, limit);
    }

    @Override
    public List<PlayerModel> selectPlayerModelList(String query, int limit) {
        List<PlayerModel> modelList = new ArrayList<>();
        try {
            ResultSet result = executeSQLQuery(query, limit);
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
                model.setWanted(result.getBoolean("is_wanted"));
                modelList.add(model);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modelList;
    }

    /**
     * Executes an SQL request for INSERT, UPDATE and DELETE
     * @param query # The query itself
     * @param params #The values to put as WHERE
     * @return # Returns the number of rows affected
     */
    private int executeSQLUpdate(String query, Object... params) throws SQLException {
        try (PreparedStatement statement = sqlConnection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            System.out.println(statement.toString());
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Executes an SQL request for SELECT
     * @param query # The query itself
     * @param params #The values to put as WHERE
     * @return # Returns the ResultSet of the request
     */
    public ResultSet executeSQLQuery(String query, Object... params) {
        try {
            PreparedStatement statement = sqlConnection.prepareStatement(query);
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
     * Executes an SQL request for CREATE TABLE
     * @param query # The query itself
     * @return # Returns if the request succeeded
     */
    public boolean executeSQL(String query, Object... params) {
        try {
            PreparedStatement statement = sqlConnection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.execute();
        } catch (SQLException e) {
            return false;
        }
    }
}
