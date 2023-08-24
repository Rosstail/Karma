package com.rosstail.karma.storage.storagetype.sql;

import com.rosstail.karma.storage.storagetype.SqlStorageRequest;

public class MySqlStorageRequest extends SqlStorageRequest {

    public MySqlStorageRequest(String pluginName) {
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


}
