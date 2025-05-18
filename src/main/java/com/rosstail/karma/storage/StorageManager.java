package com.rosstail.karma.storage;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.players.PlayerModel;
import com.rosstail.karma.storage.storagetype.SqlStorageManager;
import com.rosstail.karma.storage.storagetype.sql.MongoDbStorageManager;
import com.rosstail.karma.storage.storagetype.sql.SQLiteStorageManager;
import com.rosstail.karma.storage.storagetype.sql.MariaDbStorageManager;
import com.rosstail.karma.storage.storagetype.sql.MySqlStorageManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private static StorageManager manager;
    private final String pluginName;

    private SqlStorageManager storageRequest;

    public static Map<String, Class<? extends SqlStorageManager>> iSqlStorageRequestMap = new HashMap<>();


    static {
        iSqlStorageRequestMap.put("mariadb", MariaDbStorageManager.class);
        iSqlStorageRequestMap.put("mongodb", MongoDbStorageManager.class);
        iSqlStorageRequestMap.put("mysql", MySqlStorageManager.class);
        iSqlStorageRequestMap.put("sqlite", SQLiteStorageManager.class); // last, failsafe for AUTO
    }

    /**
     * Add custom objective from add-ons
     *
     * @param name
     * @param customStorageManagerClass
     * @return
     */
    public static boolean addCustomManager(String name, Class<? extends SqlStorageManager> customStorageManagerClass) {
        if (!iSqlStorageRequestMap.containsKey(name)) {
            iSqlStorageRequestMap.put(name, customStorageManagerClass);
            AdaptMessage.print("[karma] Custom storage " + name + " added to the list !", AdaptMessage.prints.OUT);
            return true;
        }
        return false;
    }

    public static StorageManager initStorageManage(Karma plugin) {
        if (manager == null) {
            manager = new StorageManager(plugin);
        }
        return manager;
    }

    private StorageManager(Karma plugin) {
        this.pluginName = plugin.getName().toLowerCase();
    }

    public String getUsedSystem() {
        String system = ConfigData.getConfigData().storage.storageType;

        if (iSqlStorageRequestMap.containsKey(system)) {
            return system;
        }
        return "sqlite";
    }

    public void chooseDatabase() {
        String host = ConfigData.getConfigData().storage.storageHost;
        String database = ConfigData.getConfigData().storage.storageDatabase;
        short port = ConfigData.getConfigData().storage.storagePort;
        String username = ConfigData.getConfigData().storage.storageUser;
        String password = ConfigData.getConfigData().storage.storagePass;
        String typeToPrint = LangManager.getMessage(LangMessage.STORAGE_TYPE);

        String type = getUsedSystem();

        if (type != null) {
            Class<? extends SqlStorageManager> managerClass = iSqlStorageRequestMap.get(type.toLowerCase());
            Constructor<? extends SqlStorageManager> managerConstructor;

            try {
                managerConstructor = managerClass.getDeclaredConstructor(String.class);
                storageRequest = managerConstructor.newInstance(pluginName);
                AdaptMessage.print("[karma] Using " + type + " database", AdaptMessage.prints.OUT);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in StorageManager class.", e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            storageRequest = new SQLiteStorageManager(pluginName);
        }

        AdaptMessage.print(typeToPrint.replaceAll("\\[type]", storageRequest.getName()), AdaptMessage.prints.OUT);
        storageRequest.setupStorage(host, port, database, username, password);
    }

    public void disconnect() {
        storageRequest.closeConnection();
    }

    /**
     * Insert player to the storage
     *
     * @param model
     */
    public boolean insertPlayerModel(PlayerModel model) {
        return storageRequest.insertPlayerModel(model);
    }

    /**
     * READ
     *
     * @param uuid
     */
    public PlayerModel selectPlayerModel(String uuid) {
        return storageRequest.selectPlayerModel(uuid);
    }

    /**
     * UPDATE
     *
     * @param model
     */
    public void updatePlayerModel(PlayerModel model, boolean async) {
        if (async) {
            storageRequest.updatePlayerModelAsync(model);
        } else {
            storageRequest.updatePlayerModel(model);
        }
    }

    /**
     * DELETE
     *
     * @param uuid
     */
    public void deletePlayerModel(String uuid) {
        storageRequest.deletePlayerModel(uuid);
    }

    public List<PlayerModel> selectPlayerModelListTop(int limit) {
        return storageRequest.selectPlayerModelListDesc(limit);
    }

    public List<PlayerModel> selectPlayerModelListBottom(int limit) {
        return storageRequest.selectPlayerModelListAsc(limit);
    }

    public static StorageManager getManager() {
        return manager;
    }
}
