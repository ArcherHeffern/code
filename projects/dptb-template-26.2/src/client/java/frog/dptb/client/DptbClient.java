package frog.dptb.client;

import frog.dptb.client.database.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;

import static frog.dptb.Dptb.MOD_ID;
import static java.time.LocalDateTime.now;

/**
 * TODO:
 * - Routebox regex
 * - Route messages
 * - Storing in database
 * - Display time left util button cooldown off
 * - Revenue snapshots every minute
 * - AFK detection
 * - Start/End session
 * - Display all my session stats
 */

public class DptbClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final DPTBDatabase DATABASE = new DPTBDatabase(
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
    );

    public static Optional<RunAttempt> currentRun = Optional.empty();
    public static Optional<RouteAttempt> currentRoute = Optional.empty();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Client");
        ClientReceiveMessageEvents.CHAT.register(new ChatMessageListener());
        ClientReceiveMessageEvents.GAME.register(new GameMessageListener());
        Optional<SessionFactory> maybeSessionFactory = DatabaseManager.initialize();

        if (maybeSessionFactory.isPresent()) {
            SessionFactory sessionFactory = maybeSessionFactory.get();
            sessionFactory.inTransaction(session -> {
                session.persist(new PlayerSnapshot(now(), 1));
            });
            sessionFactory.inTransaction(session -> {
                session.createSelectionQuery("From PlayerSnapshot", PlayerSnapshot.class).getResultList().forEach(result -> {
                    LOGGER.info(result.toString());
                });
            });
        } else {
            LOGGER.error("SessionFactory Could not be created.");
        }
    }
}

