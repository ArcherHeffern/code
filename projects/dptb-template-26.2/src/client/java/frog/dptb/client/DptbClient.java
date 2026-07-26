package frog.dptb.client;

import frog.dptb.client.context.DPTBContext;
import frog.dptb.client.context.DPTBContextProvider;
import frog.dptb.client.listeners.ChatMessageListener;
import frog.dptb.client.listeners.GameMessageListener;
import frog.dptb.client.utils.ComponentSupplier;
import frog.dptb.client.utils.IntegerTransformer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.resources.Identifier;
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
 *
 * COMPLETE
 * - Display countdown until button clickable
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
                (_width) -> 20,
                (height) -> 20,
                () -> {
                    Duration d = Duration.between(CONTEXT.getLastButtonPress(), LocalDateTime.now());
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
        );

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


