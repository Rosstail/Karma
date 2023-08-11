package com.rosstail.karma.datas.storage;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.storagetype.sql.MongoDbStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.sql.LiteSqlStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.sql.MariaDbStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.sql.MySqlStorageRequest;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;

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
        String typeToPrint = LangManager.getMessage(LangMessage.STORAGE_TYPE);

        switch (type) {
            case "mysql":
                AdaptMessage.print(typeToPrint.replaceAll("\\[type]", "MySQL"), AdaptMessage.prints.OUT);
                mySqlStorageRequest = new MySqlStorageRequest(pluginName);
                mySqlStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "mariadb":
                AdaptMessage.print(typeToPrint.replaceAll("\\[type]", "mariaDB"), AdaptMessage.prints.OUT);
                mariaDBStorageRequest = new MariaDbStorageRequest(pluginName);
                mariaDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "mongodb":
                AdaptMessage.print(typeToPrint.replaceAll("\\[type]", "MongoDB"), AdaptMessage.prints.OUT);
                mongoDBStorageRequest = new MongoDbStorageRequest(pluginName);
                mongoDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            default:
                AdaptMessage.print(typeToPrint.replaceAll("\\[type]", "LiteSQL"), AdaptMessage.prints.OUT);
                liteSqlDBStorageRequest = new LiteSqlStorageRequest(pluginName);
                liteSqlDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
        }

    }

    public void disconnect() {
        switch (type) {
            case "mysql":
                mySqlStorageRequest.closeConnection();
                break;
            case "mariadb":
                mariaDBStorageRequest.closeConnection();
                break;
            case "mongodb":
                mongoDBStorageRequest.closeConnection();
                break;
            default:
                liteSqlDBStorageRequest.closeConnection();
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
    public void updatePlayerModel(PlayerModel model, boolean async) {
        switch (type) {
            case "mysql":
                if (async) {
                    mySqlStorageRequest.updatePlayerModelAsync(model);
                } else {
                    mySqlStorageRequest.updatePlayerModel(model);
                }
                break;
            case "mariadb":
                if (async) {
                    mariaDBStorageRequest.updatePlayerModelAsync(model);
                } else {
                    mariaDBStorageRequest.updatePlayerModel(model);
                }
                break;
            case "mongodb":
                if (async) {
                    mongoDBStorageRequest.updatePlayerModelAsync(model);
                } else {
                    mongoDBStorageRequest.updatePlayerModel(model);
                }
                break;
            default:
                if (async) {
                    liteSqlDBStorageRequest.updatePlayerModelAsync(model);
                } else {
                    liteSqlDBStorageRequest.updatePlayerModel(model);
                }
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
                return mongoDBStorageRequest.selectPlayerModelListDesc(limit);
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
                return mongoDBStorageRequest.selectPlayerModelListAsc(limit);
            default:
                return liteSqlDBStorageRequest.selectPlayerModelListAsc(limit);
        }
    }

    public static StorageManager getManager() {
        return manager;
    }
}
