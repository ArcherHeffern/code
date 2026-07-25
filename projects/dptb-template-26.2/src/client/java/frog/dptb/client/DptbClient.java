package frog.dptb.client;

import frog.dptb.client.database.DPTBDatabase;
import frog.dptb.client.database.RouteAttempt;
import frog.dptb.client.database.RunAttempt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;

import static frog.dptb.Dptb.MOD_ID;

/**
 * TODO:
 * * Routebox regex
 * * Route messages
 * * Storing in database
 * * Display time left util button cooldown off
 * * Revenue snapshots every minute
 * * AFK detection
 * * Start/End session
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
    }
}


