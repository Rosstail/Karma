package com.rosstail.karma.datas.storage.storagetype.sql;

import com.rosstail.karma.datas.storage.storagetype.SqlStorageRequest;

import java.sql.Connection;

public class LiteSqlStorageRequest extends SqlStorageRequest {

    public LiteSqlStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.url = "jdbc:sqlite:base.db";
        try {
            Connection connection = openConnection();
            super.createKarmaTable();
            closeConnection(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
