package frog.dptb.client.database;

import frog.dptb.client.context.DPTBContext;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.Optional;

import static java.time.LocalDateTime.now;

public class DatabaseManager {

    public static Optional<SessionFactory> initialize() {
        final StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder()
                        .build();
        try {
            return Optional.of(
                    new MetadataSources(registry)
                            .addAnnotatedClass(GameSnapshotEnt.class)
                            .buildMetadata()
                            .buildSessionFactory()
                    );
        }
        catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            return Optional.empty();
        }
    }

    public static void persist(SessionFactory sessionFactory, Object o) {
        sessionFactory.inTransaction(session -> {
            session.persist(new GameSnapshotEnt(now(), 1, 1));
        });

    }

        public static void testQueries(DPTBContext context) {
        SessionFactory sessionFactory = context.getDBsessionFactory();
        sessionFactory.inTransaction(session -> {
            session.persist(new GameSnapshotEnt(now(), 1, 1));
        });
        sessionFactory.inTransaction(session -> {
            session.createSelectionQuery("From PlayerSnapshot", GameSnapshotEnt.class).getResultList().forEach(result -> {
                context.getLogger().info(result.toString());
            });
        });
    }
}