package com.rosstail.karma.storage;

import com.rosstail.karma.storage.mappers.playerdataentity.PlayerDataEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class HibernateUtil {

    public static SessionFactory buildSessionFactory(Properties settings) {
        Configuration configuration = new Configuration();
        configuration.setProperties(settings);

        configuration.addAnnotatedClass(PlayerDataEntity.class);

        StandardServiceRegistryBuilder serviceRegistryBuilder =
                new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());

        return configuration.buildSessionFactory(serviceRegistryBuilder.build());
    }
}
