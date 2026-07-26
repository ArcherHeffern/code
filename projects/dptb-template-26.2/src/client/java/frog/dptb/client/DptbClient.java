package frog.dptb.client;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.resources.Identifier;
import frog.dptb.client.database.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.time.LocalDateTime;

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

    public static DPTBContext CONTEXT = DPTBContextProvider.get();

    @Override
    public void onInitializeClient() {
        CONTEXT.getLogger().info("Initializing Client");
        ClientReceiveMessageEvents.CHAT.register(new ChatMessageListener());
        ClientReceiveMessageEvents.GAME.register(new GameMessageListener());

//        addHUDElement(
//                "testing",
//                TextAlignment.LEFT,
//                0,
//                200,
//                () -> "Hello from MyMod"
//        );
        addHUDElement(
                "button_countdown",
                TextAlignment.LEFT,
                0,
                200,
                () -> {
                    Duration d = Duration.between(CONTEXT.getLastButtonPress(), LocalDateTime.now());
                    CONTEXT.getLogger().debug(CONTEXT.getLastButtonPress().toString());
                    if (d.compareTo(Duration.ofSeconds(15)) > 0) {
                        return "BUTTON CLICKABLE!!!";
                    }
                    double totalSeconds = 15 - (d.toMillis() / 1000.0);
                    return String.format("%.1f", totalSeconds);
                }
        );

    }

    private void addHUDElement(String uniqueName, TextAlignment textAlignment, int x, int y, StringSupplier content) {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(CONTEXT.getModId(), uniqueName),
                (graphics, deltaTracker) -> {
                    var textRenderer = graphics.textRenderer();
                    ActiveTextCollector collector = graphics.textRenderer();
                    textRenderer.accept(
                            textAlignment,
                            x,
                            y,
                            collector.defaultParameters(),
                            Component.literal(content.get())
                    );
                }
        );

    }
}


