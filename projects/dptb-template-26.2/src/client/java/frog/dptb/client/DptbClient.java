package frog.dptb.client;

import frog.dptb.client.database.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.hibernate.SessionFactory;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;

import static frog.dptb.Dptb.MOD_ID;

/**
 * TODO:
 * - Route messages
 * - Display time left util button cooldown off
 * - Revenue snapshots every minute
 * - AFK detection
 * - Start/End session
 * - Display all my session stats
 * - ModImplementation in build.gradle
 */

public class DptbClient implements ClientModInitializer {

    public static DPTBContext CONTEXT = DPTBContext.get();

    @Override
    public void onInitializeClient() {
        CONTEXT.getLogger().info("Initializing Client");
        ClientReceiveMessageEvents.CHAT.register(new ChatMessageListener());
        ClientReceiveMessageEvents.GAME.register(new GameMessageListener());

//        DatabaseManager.testQueries(CONTEXT);
    }
}

