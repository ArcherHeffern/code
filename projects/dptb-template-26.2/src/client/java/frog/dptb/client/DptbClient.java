package frog.dptb.client;

import frog.dptb.client.context.DPTBContext;
import frog.dptb.client.context.DPTBContextProvider;
import frog.dptb.client.context.DPTBSession;
import frog.dptb.client.database.DatabaseManager;
import frog.dptb.client.database.SessionEnt;
import frog.dptb.client.listeners.ChatMessageListener;
import frog.dptb.client.listeners.GameMessageListener;
import frog.dptb.client.utils.ComponentSupplier;
import frog.dptb.client.utils.IntegerTransformer;
import frog.dptb.client.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * TODO:
 * - Why isn't my session being written to database?
 * - Route messages
 * - Session Statistics (Revenue/Minute, average completion length, average completion time)
 *
 * COMPLETE
 * - Display countdown until button clickable
 * - Display revenue rate metric
 * - Session Management (AFK, joining/leaving housing, exiting Hypixel)
 */

public class DptbClient implements ClientModInitializer {

    public static DPTBContext CONTEXT = DPTBContextProvider.get();

    @Override
    public void onInitializeClient() {
        Logger logger = CONTEXT.getLogger();
        logger.info("Initializing Client");
        ClientReceiveMessageEvents.CHAT.register(new ChatMessageListener());
        ClientReceiveMessageEvents.GAME.register(new GameMessageListener());

        new CronService().run();

        addHUDElement(
            "button_countdown",
            TextAlignment.LEFT,
            (_width) -> 20,
            (_height) -> 20,
            () -> CONTEXT.getSession().map(
                session -> {
                    Duration d = Duration.between(session.getLastButtonPress(), LocalDateTime.now());
                    if (d.compareTo(Duration.ofSeconds(15)) > 0) {
                        return Component.literal("BUTTON CLICKABLE.").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
                    }
                    double secondsRemaining = 15 - (d.toMillis() / 1000.0);
                    var f = Component.literal(String.format("%.1f", secondsRemaining));
                    ChatFormatting formatting = ChatFormatting.GREEN;
                    if (secondsRemaining < 7) {
                        formatting = ChatFormatting.YELLOW;
                    }
                    if (secondsRemaining < 3) {
                        formatting = ChatFormatting.RED;
                    }
                    return f.withStyle(formatting);
                }
            ).orElse(Component.empty())
        );

        addHUDElement(
                "revenue_per_minute",
                TextAlignment.LEFT,
                (_width) -> 20,
                (_height) -> 40,
                () -> CONTEXT.getSession().map(
                    session -> {
                        Duration d = session.getSessionDuration();
                        float minutesElapsed = (float) (d.toMillis() / 1000.0 / 60);
                        float revenuePerMinute = session.getTotalRevenue() / minutesElapsed;
                        return Component.literal(String.format("Revenue/Minute: %.1f", revenuePerMinute));
                    }
                ).orElse(Component.empty())
        );

        // If user disconnects from Hypixel - End the session
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CONTEXT.getSession().ifPresent(session -> {
                if (Utils.isHypixel(client)) {
                    logger.debug("Disconnecting from Hypixel");
                    SessionEnt s = new SessionEnt(session.getDbSessionStart().get(), LocalDateTime.now());
                    DatabaseManager.persist(CONTEXT.getDBsessionFactory(), s);
                    CONTEXT.setSession(Optional.empty());
                }
            });
        });

        // If user exits DPTB Housing - Update session.
        // Detect leaving Housing (e.g., going back to lobby or switching games)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            if (!Utils.isHypixel(client)) return;
            // Optimize performance by scanning every 10 ticks instead of every single tick
            CONTEXT.setTickCount((CONTEXT.getTickCount() + 1) % 10);
            if (CONTEXT.getTickCount() != 0) return;
            boolean wasInHousingForAFewSeconds = CONTEXT.getSession().map(
                    s -> s.getDbSessionStart().map(
                            start -> {
                            Duration between = Duration.between(start, LocalDateTime.now());
                            return between.compareTo(Duration.ofSeconds(5)) > 0;
                        }
                ).orElse(false)).orElse(false);
            if (!wasInHousingForAFewSeconds) return;
            boolean isInHousingNow = Utils.checkIfInDPTB(client);
            if (isInHousingNow) return; // Maybe the scoreboard is being loaded late - meaning we then end the session early

            logger.debug("Leaving housing.");
            DPTBSession session = CONTEXT.getSession().get();
            session.pause(CONTEXT.getDBsessionFactory(), false);
        });
    }


    private void addHUDElement(String uniqueName, TextAlignment textAlignment, IntegerTransformer x, IntegerTransformer y, ComponentSupplier content) {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(CONTEXT.getModId(), uniqueName),
                (graphics, deltaTracker) -> {
                    var window = Minecraft.getInstance().getWindow();
                    var width = window.getScreenWidth();
                    var height = window.getScreenHeight();

                    var textRenderer = graphics.textRenderer();
                    ActiveTextCollector collector = graphics.textRenderer();
                    textRenderer.accept(
                            textAlignment,
                            x.get(width),
                            y.get(height),
                            collector.defaultParameters(),
                            content.get()
                    );
                }
        );
    }
}