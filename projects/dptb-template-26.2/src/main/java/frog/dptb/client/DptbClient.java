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
import frog.dptb.client.utils.MathSolver;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TODO:
 * Metric aggregation
 * Telemetry if I want to publish
 *
 * Additional Metrics
 * - Net income / Minute
 * - Route income / Minute (Excludes time at spawn)
 * - Run income / Minute (Excludes time at spawn)
 *
 * Stretch Tasks
 * - Create a Marketplace + Bots to handle transactions
 * - Pay for AFK player
 *
 * COMPLETED
 * - Display countdown until button clickable
 * - Display session statistics
 * - Session Management (AFK, joining/leaving housing, exiting Hypixel)
 * - Stores all successful and unsuccessful User Run and Route attempts in local database (See RunAttemptEnt)
 * - Stores Number of user online and timestamp every minute (See GameSnapshotEnt)
 * - AutoCheer
 * - Math Center Mod
 * - Metrics aren't collected when user joins until they start a run
 * - Auto detects if user is running around at spawn (No runs started for 10 seconds) and pauses metrics. Metrics resume once they attempt a run.
 * - Auto detects if user gets AFK'ed and pauses metrics
 */

public class DptbClient implements ClientModInitializer {

    public static final String MOD_ID = "dptb";
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
                    if (Utils.isGreaterThan(d, Duration.ofSeconds(15))) {
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
                "session_time",
                TextAlignment.LEFT,
                (_width) -> 20,
                (_height) -> 40,
                () -> CONTEXT.getSession().map(
                        session -> {
                            Duration duration = session.getSessionDuration();
                            int hours = duration.toHoursPart();
                            int minutes = duration.toMinutesPart();
                            int seconds = duration.toSecondsPart();
                            String isActiveMsg = session.getDbSessionStart().isPresent() ? "ACTIVE": "PAUSED";
                            return Component.literal(String.format("[%s] %02d:%02d:%02d", isActiveMsg, hours, minutes, seconds));
                        }
                ).orElse(
                        Component.empty()
                )
        );

        addHUDElement(
                "revenue_per_minute",
                TextAlignment.LEFT,
                (_width) -> 20,
                (_height) -> 60,
                () -> CONTEXT.getSession().map(
                    session -> {
                        Duration d = session.getSessionDuration();
                        float minutesElapsed = (float) (d.toMillis() / 1000.0 / 60);
                        int revenuePerMinute = (int)(session.getTotalRevenue() / minutesElapsed);
                        return Component.literal(String.format("Revenue/Minute: %d", revenuePerMinute));
                    }
                ).orElse(Component.empty())
        );

        addHUDElement(
                "math_solution",
                TextAlignment.LEFT,
                (_width) -> 20,
                (_height) -> 80,
                () -> CONTEXT.getSession().map(
                        session -> session.getMathSolution().map(
                                (solution) -> Component.literal(Float.toString(solution))
                        ).orElse(
                                Component.empty()
                        )
                ).orElse(Component.empty())
        );

        // If user disconnects from Hypixel - End the session
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CONTEXT.getSession().ifPresent(session -> {
                if (Utils.isHypixel(client)) {
                    logger.debug("Disconnecting from Hypixel. Ending session.");
                    SessionEnt s = new SessionEnt(session.getDbSessionStart().get(), LocalDateTime.now());
                    DatabaseManager.persist(CONTEXT.getDBsessionFactory(), s);
                    CONTEXT.setSession(Optional.empty());
                }
            });
        });

        // If user exits DPTB Housing - Update session.
        // Detect leaving Housing (e.g., going back to lobby or switching games)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Optimize performance by scanning every 10 ticks instead of every single tick
            CONTEXT.setTickCount((CONTEXT.getTickCount() + 1) % 10);
            if (CONTEXT.getTickCount() != 0) return;

            if (!Utils.isHypixel(client)) return;
            if (!Utils.wasOrIsInDPTB()) return;
            if (Utils.checkIfInDPTB(client)) return;

            logger.debug("Leaving housing.");
            DPTBSession session = CONTEXT.getSession().get();
            session.pause(CONTEXT.getDBsessionFactory(), Optional.empty());
            session.setLastButtonPress(LocalDateTime.MIN);

            // Exiting housing ends your route, even if it hasn't started yet.
            session.setRunningRoute(false);
        });

        // If user hasn't started a run for 10 seconds - Stop Session
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Optimize performance by scanning every 10 ticks instead of every single tick
            if (CONTEXT.getTickCount() != 0) return;

            CONTEXT.getSession().ifPresent((dptbSession -> {
                if (dptbSession.isPaused()) {
                    return;
                }
                if (dptbSession.getRunAttemptEntityBuilder().isPresent()) {
                    return;
                }
                dptbSession.getLastRunAttempt().ifPresentOrElse(
                        (lastRunAttempt) -> {
                            Duration lookBack = Duration.ofSeconds(10);
                            if (Utils.isLessThan(
                                    Duration.between(lastRunAttempt, LocalDateTime.now()),
                                    lookBack)
                            ) {
                                return;
                            }
                            dptbSession.pause(CONTEXT.getDBsessionFactory(), Optional.of(lookBack));
                        },
                        () -> dptbSession.pause(CONTEXT.getDBsessionFactory(), Optional.empty())
                );
            }));
        });

        // Math Center Solver
        MathSolver mathSolver = new MathSolver();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (CONTEXT.getTickCount() != 0) return;
            CONTEXT.getSession().ifPresent((session) -> {
                Optional<Integer> maybeResult = mathSolver.trySolve(client, session, logger).map(Float::intValue);
                session.setMathSolution(maybeResult);
            });
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