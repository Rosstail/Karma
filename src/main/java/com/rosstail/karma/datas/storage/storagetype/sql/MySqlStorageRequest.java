package com.rosstail.karma.datas.storage.storagetype.sql;

import com.rosstail.karma.datas.storage.storagetype.SqlStorageRequest;

import java.sql.DriverManager;

public class MySqlStorageRequest extends SqlStorageRequest {

    public MySqlStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            setConnection(DriverManager.getConnection(url, username, password));
            super.setupStorage(host, port, database, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
