package com.rosstail.karma.datas.storage.storagetype.sql;

import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.storagetype.SqlStorageRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class MongoDbStorageRequest extends SqlStorageRequest {

    public MongoDbStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:mongodb://" + host + ":" + port + "/" + database;
        this.driver = "mongodb.jdbc.MongoDriver";
        this.username = username;
        this.password = password;
        try {
            Connection connection = openConnection();
            super.createKarmaTable();
            closeConnection(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean insertPayerModel(PlayerModel model) {
        return false;
    }

    @Override
    public PlayerModel selectPlayerModel(String uuid) {
        return null;
    }

    @Override
    public void updatePlayerModel(PlayerModel model) {

    }

    @Override
    public void deletePlayerModel(String uuid) {

    }

    @Override
    public List<PlayerModel> selectPlayerModelList(String order, int limit) {
        return null;
    }

    public void disconnect() {
    }
}
