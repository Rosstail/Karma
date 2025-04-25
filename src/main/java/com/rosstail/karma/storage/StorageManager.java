package com.rosstail.karma.storage;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.players.PlayerDataModel;
import com.rosstail.karma.storage.mappers.playerdataentity.PlayerDataEntity;
import com.rosstail.karma.storage.mappers.playerdataentity.PlayerDataMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {
    private static StorageManager manager;

    private final Karma plugin;
    private final String pluginName;
    private SessionFactory sessionFactory;
    private final Set<PlayerDataEntity> pendingUpdates = ConcurrentHashMap.newKeySet();

    public static StorageManager initStorageManage(Karma plugin) {
        if (manager == null) {
            manager = new StorageManager(plugin);
        }
        return manager;
    }

    private StorageManager(Karma plugin) {
        this.plugin = plugin;
        this.pluginName = plugin.getName().toLowerCase();
    }

    public void connect() {
        Properties settings = new Properties();
        switch (ConfigData.getConfigData().storage.storageType) {
            case "mysql":
                // TODO: implement MySQL configuration
                break;
            default: // SQLITE
                File dbFile = new File(plugin.getDataFolder(), "data/data.db");
                settings.put("hibernate.dialect", "org.hibernate.dialect.SQLiteDialect");
                settings.put("hibernate.connection.driver_class", "org.sqlite.JDBC");
                settings.put("hibernate.connection.url", "jdbc:sqlite:" + dbFile.getAbsolutePath());
                settings.put("hibernate.hbm2ddl.auto", "update");
                break;
        }

        sessionFactory = HibernateUtil.buildSessionFactory(settings);
    }

    public void disconnect() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public boolean uploadPlayerModel(PlayerDataModel model) {
        Transaction tx = null;
        PlayerDataEntity playerDataEntity = PlayerDataMapper.toEntity(model);

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(playerDataEntity);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            AdaptMessage.print("StorageManager#uploadPlayerModel - erreur", AdaptMessage.prints.ERROR);
            e.printStackTrace();
        }
        return false;
    }

    public boolean asyncUploadPlayerModel(PlayerDataModel model) {
        return CompletableFuture.supplyAsync(() -> uploadPlayerModel(model)).join();
    }

    public void queueUserForUpdate(PlayerDataEntity entity) {
        pendingUpdates.removeIf(playerDataEntity -> playerDataEntity.getUuid().equalsIgnoreCase(entity.getUuid()));
        pendingUpdates.add(entity);
    }

    public void flushPendingUpdates() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            for (PlayerDataEntity entity : pendingUpdates) {
                session.merge(entity);
            }
            session.getTransaction().commit();
            pendingUpdates.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PlayerDataModel selectPlayerModel(String uuid) {
        try (Session session = sessionFactory.openSession()) {
            PlayerDataEntity playerDataEntity =
                    session.createQuery("FROM karma WHERE uuid = :uuid", PlayerDataEntity.class)
                            .setParameter("uuid", uuid)
                            .uniqueResult();

            if (playerDataEntity != null) {
                return PlayerDataMapper.toProfile(playerDataEntity);
            }
        } catch (Exception e) {
            AdaptMessage.print("StorageManager#selectPlayerModel - erreur", AdaptMessage.prints.ERROR);
            e.printStackTrace();
        }
        return null;
    }

    public boolean deletePlayerModel(String uuid) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
            PlayerDataEntity playerDataEntity =
                    session.createQuery("FROM karma WHERE uuid = :uuid", PlayerDataEntity.class)
                            .setParameter("uuid", uuid)
                            .uniqueResult();

            if (playerDataEntity != null) {
                tx = session.beginTransaction();
                session.remove(playerDataEntity);
                tx.commit();
                return true;
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            AdaptMessage.print("StorageManager#deletePlayerModel - erreur", AdaptMessage.prints.ERROR);
            e.printStackTrace();
        }
        return false;
    }

    public List<PlayerDataModel> selectPlayerModelListTop(int limit) {
        try (Session session = sessionFactory.openSession()) {
            List<PlayerDataEntity> playerDataEntityList =
                    session.createQuery("FROM karma ORDER BY karma DESC", PlayerDataEntity.class)
                            .setMaxResults(limit)
                            .list();

            return playerDataEntityList.stream().map(PlayerDataMapper::toProfile).toList();
        } catch (Exception e) {
            AdaptMessage.print("StorageManager#selectPlayerModelListTop - erreur", AdaptMessage.prints.ERROR);
            e.printStackTrace();
        }
        return null;
    }

    public List<PlayerDataModel> selectPlayerModelListBottom(int limit) {
        try (Session session = sessionFactory.openSession()) {
            List<PlayerDataEntity> playerDataEntityList =
                    session.createQuery("FROM karma ORDER BY karma ASC", PlayerDataEntity.class)
                            .setMaxResults(limit)
                            .list();

            return playerDataEntityList.stream().map(PlayerDataMapper::toProfile).toList();
        } catch (Exception e) {
            AdaptMessage.print("StorageManager#selectPlayerModelListBottom - erreur", AdaptMessage.prints.ERROR);
            e.printStackTrace();
        }
        return null;
    }

    public static StorageManager getManager() {
        return manager;
    }
}
