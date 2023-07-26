package com.rosstail.karma.datas.storage.storagetype;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlStorageRequest implements StorageRequest {
    private final String pluginName;
    private Connection connection;

    public MySqlStorageRequest(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        try {
            // Connexion à la base de données SQL
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, username, password);
            createKarmaTable();
        } catch (Exception e) {
            e.printStackTrace();
            // Gérer les erreurs de connexion ici
        }
    }

    public void disconnect() {
        // Ferme les connexions à la base de données si nécessaire
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        /*
        if (mongoClient != null) {
            mongoClient.close();
        }
         */
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
            ResultSet result = executeSQLQuery(query, uuid);
            if (result.next()) {
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
                model.setWanted(result.getBoolean("is_wanted"));
                return model;
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

    public List<PlayerModel> selectPlayerModelListAsc(int limit) {
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
        query += " ORDER BY " + pluginName +  ".karma ASC LIMIT ?";
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
        query += " ORDER BY " + pluginName +  ".karma DESC LIMIT ?";
        return selectPlayerModelList(query, limit);
    }

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
                model.setWanted(model.getWantedTimeStamp().getTime() > System.currentTimeMillis());
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
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
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
     * @param query # The query itself
     * @return # Returns if the request succeeded
     */
    public boolean executeSQL(String query, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.execute();
        } catch (SQLException e) {
            return false;
        }
    }
}
