package com.rosstail.karma.datas.storage;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.storagetype.LiteSQLStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.MariaDBStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.MongoDBStorageRequest;
import com.rosstail.karma.datas.storage.storagetype.SQLStorageRequest;

import java.util.List;

public class StorageManager {
    private static StorageManager manager;
    private final String pluginName;
    private String type;
    public String host, database, username, password;
    public short port;

    private SQLStorageRequest sqlStorageRequest;
    private MariaDBStorageRequest mariaDBStorageRequest;
    private MongoDBStorageRequest mongoDBStorageRequest;
    private LiteSQLStorageRequest liteSqlDBStorageRequest;

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
            case "sql":
                System.out.println("choose SQL");
                sqlStorageRequest = new SQLStorageRequest(pluginName);
                sqlStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "mariadb":
                System.out.println("Choose MariaDB");
                mariaDBStorageRequest = new MariaDBStorageRequest(pluginName);
                mariaDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "mongodb":
                System.out.println("Choose MongoDB");
                mongoDBStorageRequest = new MongoDBStorageRequest(pluginName);
                mongoDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            case "litesql":
                System.out.println("Choose LiteSQL");
                liteSqlDBStorageRequest = new LiteSQLStorageRequest(pluginName);
                liteSqlDBStorageRequest.setupStorage(host, port, database, username, password);
                break;
            default:
                System.out.println("Choose LocalStorage");
                break;
        }

    }

    public void disconnect() {
        switch (type) {
            case "sql":
                sqlStorageRequest.disconnect();
                break;
            case "mariadb":
                mariaDBStorageRequest.disconnect();
                break;
            case "mongodb":
                mongoDBStorageRequest.disconnect();
                break;
            case "LiteSQL":
                liteSqlDBStorageRequest.disconnect();
                break;
            default:
                System.out.println("LocalStorage");
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
            case "sql":
                return sqlStorageRequest.insertPayerModel(model);
            case "mariadb":
                return mariaDBStorageRequest.insertPayerModel(model);
            case "mongodb":
                return mongoDBStorageRequest.insertPayerModel(model);
            case "litesql":
                return liteSqlDBStorageRequest.insertPayerModel(model);
            default:
                System.out.println("LocalStorage");
        }
        return false;
    }

    /**
     * READ
     *
     * @param uuid
     */
    public PlayerModel selectPlayerModel(String uuid) {
        switch (type) {
            case "sql":
                return sqlStorageRequest.selectPlayerModel(uuid);
            case "mariadb":
                return mariaDBStorageRequest.selectPlayerModel(uuid);
            case "mongodb":
                return mongoDBStorageRequest.selectPlayerModel(uuid);
            case "litesql":
                return liteSqlDBStorageRequest.selectPlayerModel(uuid);
            default:
                //TODO return mariadbStorageRequest.selectPlayerModel(uuid);
                return null;
        }
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updatePlayerModel(PlayerModel model) {
        switch (type) {
            case "sql":
                sqlStorageRequest.updatePlayerModel(model);
                break;
            case "mariadb":
                mariaDBStorageRequest.updatePlayerModel(model);
                break;
            case "mongodb":
                mongoDBStorageRequest.updatePlayerModel(model);
                break;
            case "litesql":
                liteSqlDBStorageRequest.updatePlayerModel(model);
                break;
            default:
                //TODO localStorageRequest.updatePlayerModel(model);
        }
    }

    /**
     * DELETE
     *
     * @param uuid
     */
    public void deletePlayerModel(String uuid) {
        switch (type) {
            case "sql":
                sqlStorageRequest.deletePlayerModel(uuid);
                break;
            case "mariadb":
                mariaDBStorageRequest.deletePlayerModel(uuid);
                break;
            case "mongodb":
                mongoDBStorageRequest.deletePlayerModel(uuid);
                break;
            case "litesql":
                liteSqlDBStorageRequest.deletePlayerModel(uuid);
                break;
            default:
                //TODO localStorageRequest.deletePlayerModel(uuid);
        }
    }

    public List<PlayerModel> selectPlayerModelListTop(int limit) {
        switch (type) {
            case "sql":
                return sqlStorageRequest.selectPlayerModelListDesc(limit);
            case "mariadb":
                return mariaDBStorageRequest.selectPlayerModelListDesc(limit);
            case "mongodb":
                return mongoDBStorageRequest.selectPlayerModelList("ASC", limit);
            case "litesql":
                return liteSqlDBStorageRequest.selectPlayerModelList("ASC", limit);
            default:
                return null;
                //TODO localStorageRequest.deletePlayerModel(uuid);
        }
    }

    public List<PlayerModel> selectPlayerModelListBottom(int limit) {
        switch (type) {
            case "sql":
                return sqlStorageRequest.selectPlayerModelListAsc(limit);
            case "mariadb":
                return mariaDBStorageRequest.selectPlayerModelListAsc(limit);
            case "mongodb":
                //return mongoDBStorageRequest.selectPlayerModelList("DESC", limit);
            case "litesql":
                //return liteSqlDBStorageRequest.selectPlayerModelList("DESC", limit);
            default:
                return null;
            //TODO localStorageRequest.deletePlayerModel(uuid);
        }
    }

    // Exemple de méthode d'écriture dans la base de données de stockage local
    public void writeToLocalStorage(String key, Object data) {
        // Insérez le code d'écriture dans la base de données de stockage local ici
    }

    public static StorageManager getManager() {
        return manager;
    }
}
