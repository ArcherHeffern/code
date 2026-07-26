package frog.dptb.client.database;

import frog.dptb.client.DptbClient;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class DatabaseManager {
    public static Optional<SessionFactory> initialize() {
        try {
            Properties properties = new Properties();

            // Forces Minecraft's Mod ClassLoader to read your jar resource
            try (InputStream stream = DatabaseManager.class.getClassLoader()
                    .getResourceAsStream("hibernate.properties")) {

                if (stream == null) {
                    throw new RuntimeException("Could not find hibernate.properties in resources!");
                }
                properties.load(stream);
            }

            Configuration configuration = new Configuration();

            // Programmatically inject the properties into the configuration setup
            configuration.setProperties(properties);

            // Register your database entity models here
            configuration.addAnnotatedClass(PlayerSnapshot.class);

            return Optional.of(configuration.buildSessionFactory());

        } catch (Exception e) {
            DptbClient.LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }
}