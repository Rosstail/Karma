package com.rosstail.karma.datas.storage;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.storagetype.LiteSqlStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.MariaDbStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.MongoDbStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.MySqlStorageRequest;

import java.util.List;

public class StorageManager {
    private static StorageManager manager;
    private final String pluginName;
    private String type;
    public String host, database, username, password;
    public short port;

    private MySqlStorageRequest mySqlStorageRequest;
    private MariaDbStorageRequest mariaDBStorageRequest;
    private MongoDbStorageRequest mongoDBStorageRequest;
    private LiteSqlStorageRequest liteSqlDBStorageRequest;

    public static StorageManager initStorageManage(Karma plugin) {
        if (manager == null) {
            manager = new StorageManager(plugin);
        }
        return manager;
    }

    private StorageManager(Karma plugin) {
        this.pluginName = plugin.getName().toLowerCase();
    }

    public void chooseDatabase() {
        host = ConfigData.getConfigData().storage.storageHost;
        database = ConfigData.getConfigData().storage.storageDatabase;
        port = ConfigData.getConfigData().storage.storagePort;
        username = ConfigData.getConfigData().storage.storageUser;
        password = ConfigData.getConfigData().storage.storagePass;
        type = ConfigData.getConfigData().storage.storageType.toLowerCase();
        switch (type) {
            case "mysql":
                System.out.println("choose MySQL");
                mySqlStorageRequest = new MySqlStorageRequest(pluginName);
                mySqlStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "mariadb":
                System.out.println("Choose MariaDB");
                mariaDBStorageRequest = new MariaDbStorageRequest(pluginName);
                mariaDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "mongodb":
                System.out.println("Choose MongoDB");
                mongoDBStorageRequest = new MongoDbStorageRequest(pluginName);
                mongoDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            default:
                System.out.println("Choose LiteSQL");
                liteSqlDBStorageRequest = new LiteSqlStorageRequest(pluginName);
                liteSqlDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
        }

    }

    public void disconnect() {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.disconnect();
                break;
            case "mariadb":
                mariaDBStorageRequest.disconnect();
                break;
            case "mongodb":
                mongoDBStorageRequest.disconnect();
                break;
            default:
                liteSqlDBStorageRequest.disconnect();
                break;
        }
    }

    /**
     * Insert player to the storage
     *
     * @param model
     */
    public boolean insertPlayerModel(PlayerModel model) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.insertPayerModel(model);
            case "mariadb":
                return mariaDBStorageRequest.insertPayerModel(model);
            case "mongodb":
                return mongoDBStorageRequest.insertPayerModel(model);
            default:
                return liteSqlDBStorageRequest.insertPayerModel(model);
        }
    }

    /**
     * READ
     *
     * @param uuid
     */
    public PlayerModel selectPlayerModel(String uuid) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectPlayerModel(uuid);
            case "mariadb":
                return mariaDBStorageRequest.selectPlayerModel(uuid);
            case "mongodb":
                return mongoDBStorageRequest.selectPlayerModel(uuid);
            default:
                return liteSqlDBStorageRequest.selectPlayerModel(uuid);
        }
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updatePlayerModel(PlayerModel model) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.updatePlayerModel(model);
                break;
            case "mariadb":
                mariaDBStorageRequest.updatePlayerModel(model);
                break;
            case "mongodb":
                mongoDBStorageRequest.updatePlayerModel(model);
                break;
            default:
                liteSqlDBStorageRequest.updatePlayerModel(model);
                break;
        }
    }

    /**
     * DELETE
     *
     * @param uuid
     */
    public void deletePlayerModel(String uuid) {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.deletePlayerModel(uuid);
                break;
            case "mariadb":
                mariaDBStorageRequest.deletePlayerModel(uuid);
                break;
            case "mongodb":
                mongoDBStorageRequest.deletePlayerModel(uuid);
                break;
            default:
                liteSqlDBStorageRequest.deletePlayerModel(uuid);
                break;
        }
    }

    public List<PlayerModel> selectPlayerModelListTop(int limit) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectPlayerModelListDesc(limit);
            case "mariadb":
                return mariaDBStorageRequest.selectPlayerModelListDesc(limit);
            case "mongodb":
                return mongoDBStorageRequest.selectPlayerModelList("ASC", limit);
            default:
                return liteSqlDBStorageRequest.selectPlayerModelListDesc(limit);
        }
    }

    public List<PlayerModel> selectPlayerModelListBottom(int limit) {
        switch (type) {
            case "mysql":
                return mySqlStorageRequest.selectPlayerModelListAsc(limit);
            case "mariadb":
                return mariaDBStorageRequest.selectPlayerModelListAsc(limit);
            case "mongodb":
                return mongoDBStorageRequest.selectPlayerModelList("DESC", limit);
            default:
                return liteSqlDBStorageRequest.selectPlayerModelListAsc(limit);
        }
    }

    public static StorageManager getManager() {
        return manager;
    }
}
