package net.zerofill.repositories;

import net.zerofill.domains.User;
import net.zerofill.domains.Weapon;
import net.zerofill.utils.DataUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public abstract class BaseRepository {
    // Method Used To Create The Hibernate's SessionFactory Object
    private static SessionFactory sessionFactory = null;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            return sessionFactory;
        }
        // Creating Configuration Instance & Passing Hibernate Configuration File
        Configuration configObj = new Configuration();
        configObj.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configObj.setProperty("hibernate.connection.driver_class", DataUtils.DB_DRIVER);
        configObj.setProperty("hibernate.connection.url", DataUtils.DB_CONNECTION);
        configObj.setProperty("hibernate.connection.username", "");
        configObj.setProperty("hibernate.connection.password", "");
        configObj.setProperty("hibernate.hbm2ddl.auto", "update");
        configObj.setProperty("hibernate.format_sql", "false");
        configObj.setProperty("hibernate.show_sql", "false");

        configObj.addAnnotatedClass(User.class);
        configObj.addAnnotatedClass(Weapon.class);

        ServiceRegistry serviceRegistryObj = new StandardServiceRegistryBuilder().applySettings(configObj.getProperties()).build();

        sessionFactory = configObj.buildSessionFactory(serviceRegistryObj);
        return sessionFactory;
    }
}
