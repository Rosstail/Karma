package com.rosstail.karma.datas.storage.storagetype.sql;

import com.rosstail.karma.datas.storage.storagetype.SqlStorageRequest;

import java.sql.DriverManager;

public class LiteSqlStorageRequest extends SqlStorageRequest {

    public LiteSqlStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        try {
            String url = "jdbc:sqlite:base.db";
            setConnection(DriverManager.getConnection(url));
            super.setupStorage(host, port, database, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
