package com.rosstail.karma.datas.storage.storagetype.sql;

import com.rosstail.karma.datas.storage.storagetype.SqlStorageRequest;

import java.sql.*;

public class MariaDbStorageRequest extends SqlStorageRequest {

    public MariaDbStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.driver = "com.mariadb.jdbc.Driver";
        this.url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
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
}
