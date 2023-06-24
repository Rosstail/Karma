package com.rosstail.karma.datas.storage.storagetype;

import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;

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
                " karma double," +
                " previous_karma double," +
                " tier varchar(50)," +
                " previous_tier varchar(50)," +
                " wanted_time bigint UNSIGNED DEFAULT 0," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);";
        executeSQL(query);
    }

    @Override
    public void insertPayerModel(PlayerModel model) {
        String query = "INSERT INTO " + pluginName + " (uuid, karma, previous_karma, tier, previous_tier, wanted_time)"
                + " VALUES (?, ?, ?, ?, ?, ?);";

        String uuid = model.getUuid();
        double karma = model.getKarma();
        double previousKarma = model.getPreviousKarma();
        String tierName = model.getTierName();
        String previousTierName = model.getPreviousTierName();
        long wantedTimeStamp = model.getWantedTimeStamp().getTime();
        try {
            boolean success = executeSQLUpdate(query, uuid, karma, previousKarma, tierName, previousTierName, wantedTimeStamp) > 0;
            System.out.println("INSERT SUCCESS " + success);
        } catch (SQLException e) {
            e.printStackTrace();
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
                model.setKarma(result.getDouble("karma"));
                model.setPreviousKarma(result.getDouble("previous_karma"));
                model.setTierName(result.getString("tier"));
                model.setPreviousTierName(result.getString("previous_tier"));
                model.setLastUpdate(result.getTimestamp("last_update").getTime());
                model.setWantedTimeStamp(new Timestamp(model.getLastUpdate() + result.getLong("wanted_time"))); //A modifier
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
        String query = "UPDATE " + pluginName + " SET karma = ?, previous_karma = ?, tier = ?, previous_tier = ?, wanted_time = ? WHERE uuid = ?";
        try {
            boolean success = executeSQLUpdate(query, model.getKarma(), model.getPreviousKarma(), model.getTierName(),model.getPreviousTierName(), model.getWantedTimeStamp().getTime(), model.getUuid())
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
        String query = "SELECT * FROM " + pluginName + " ORDER BY " + pluginName +  ".karma DESC LIMIT ?";
        return selectPlayerModelList(query, limit);
    }

    public List<PlayerModel> selectPlayerModelListDesc(int limit) {
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
                model.setKarma(result.getDouble("karma"));
                model.setPreviousKarma(result.getDouble("previous_karma"));
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
