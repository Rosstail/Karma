package com.rosstail.karma.datas.storage.storagetype;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.rosstail.karma.datas.PlayerModel;

import java.util.List;

public class MongoDBStorageRequest implements StorageRequest {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDBStorageRequest(String pluginName) {

    }
    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        try {
            // Connexion à la base de données MongoDB
            //mongoClient = new MongoClient(host, port);
            //mongoDatabase = mongoClient.getDatabase(database);
        } catch (Exception e) {
            e.printStackTrace();
            // Gérer les erreurs de connexion ici
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
