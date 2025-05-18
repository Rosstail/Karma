package com.rosstail.karma.storage.storagetype.sql;

import com.rosstail.karma.storage.storagetype.SqlStorageManager;

public class MariaDbStorageManager extends SqlStorageManager {

    public MariaDbStorageManager(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.driver = "com.mariadb.jdbc.Driver";
        this.url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;

        super.createKarmaTable();
    }

    @Override
    public String getName() {
        return "MariaDB";
    }
}
