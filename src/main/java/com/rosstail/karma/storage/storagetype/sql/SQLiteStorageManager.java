package com.rosstail.karma.storage.storagetype.sql;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.players.PlayerDataManager;
import com.rosstail.karma.players.PlayerModel;
import com.rosstail.karma.storage.storagetype.SqlStorageManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SQLiteStorageManager extends SqlStorageManager {

    public SQLiteStorageManager(String pluginName) {
        super(pluginName);
    }

    @Override
    public void createKarmaTable() {
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
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:sqlite:./plugins/Karma/playerdata/playerdata.db";
        this.createKarmaTable();

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
                model.setLastUpdate(lastUpdateUTC + getOffsetMillis());

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
    public String getName() {
        return "SQLite";
    }
}
