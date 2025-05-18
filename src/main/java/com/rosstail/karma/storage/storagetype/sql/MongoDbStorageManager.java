package com.rosstail.karma.storage.storagetype.sql;

import com.rosstail.karma.storage.storagetype.SqlStorageManager;

public class MongoDbStorageManager extends SqlStorageManager {

    public MongoDbStorageManager(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:mongodb://" + host + ":" + port + "/" + database;
        this.driver = "mongodb.jdbc.MongoDriver";
        this.username = username;
        this.password = password;

        super.createKarmaTable();
    }

    @Override
    public String getName() {
        return "MongoDB";
    }
}
