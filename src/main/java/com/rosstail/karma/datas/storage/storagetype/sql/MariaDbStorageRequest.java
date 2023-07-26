package com.rosstail.karma.datas.storage.storagetype.sql;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.storagetype.SqlStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.StorageRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MariaDbStorageRequest extends SqlStorageRequest {

    public MariaDbStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        try {
            Class.forName("com.mariadb.jdbc.Driver");
            String url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
            setConnection(DriverManager.getConnection(url, username, password));
            super.setupStorage(host, port, database, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
