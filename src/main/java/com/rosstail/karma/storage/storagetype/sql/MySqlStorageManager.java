package com.rosstail.karma.storage.storagetype.sql;

import com.rosstail.karma.storage.storagetype.SqlStorageManager;

public class MySqlStorageManager extends SqlStorageManager {

    public MySqlStorageManager(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.driver = "com.mysql.jdbc.Driver";
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;

        super.createKarmaTable();
    }

    @Override
    public String getName() {
        return "MySQL";
    }
}
