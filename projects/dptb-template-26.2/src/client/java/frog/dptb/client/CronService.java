package frog.dptb.client;

import frog.dptb.client.context.DPTBContext;
import frog.dptb.client.context.DPTBContextProvider;
import frog.dptb.client.database.GameSnapshotEnt;
import frog.dptb.client.utils.HypixelSidebarReader;
import frog.dptb.client.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CronService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Minecraft client;
    final private DPTBContext CONTEXT = DPTBContextProvider.get();

    private static final Pattern PLAYERS_ONLINE_IN_SIDEBAR_REGEX = Pattern.compile(
            "^\\[[IVXCM-]+] (?<playersonline>\\d+)✌$"
    );
    private static final Pattern GOLD_IN_SIDEBAR_REGEX = Pattern.compile(
            "^⛂ Gold: (?<gold>[0-9,]+)$"
    );

    private static final int PLAYERS_ONLINE_IN_SIDEBAR_INDEX = 4;
    private static final int GOLD_IN_SIDEBAR_INDEX = 5;

    public void run() {
        // Capture the server instance when it starts up
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            this.client = client;
        });

        // Start the async timer loop
        scheduler.scheduleAtFixedRate(this::asyncTimerTrigger, 1, 1, TimeUnit.MINUTES);
    }

    private void asyncTimerTrigger() {
        if (client == null) return;
        if (!Utils.checkIfInDPTB(client)) return;

        // Jump onto the main thread to safely read the scoreboard
        client.execute(() -> {
            List<String> sidebar = HypixelSidebarReader.getVisualLines(client);
            int gold = -1;
            int numPlayersOnline = -1;
            if (sidebar.size() > GOLD_IN_SIDEBAR_INDEX) {
                String maybeGoldLine = sidebar.get(GOLD_IN_SIDEBAR_INDEX);
                Matcher goldLineMatcher = GOLD_IN_SIDEBAR_REGEX.matcher(maybeGoldLine);
                if (goldLineMatcher.matches()) {
                    gold = Integer.parseInt(goldLineMatcher.group("gold").replace(",", ""));
                }
            }
            if (sidebar.size() > PLAYERS_ONLINE_IN_SIDEBAR_INDEX) {
                String maybeNumPlayersOnline = sidebar.get(PLAYERS_ONLINE_IN_SIDEBAR_INDEX);
                Matcher numPlayersOnlineMatcher = PLAYERS_ONLINE_IN_SIDEBAR_REGEX.matcher(maybeNumPlayersOnline);
                if (numPlayersOnlineMatcher.matches()) {
                    numPlayersOnline = Integer.parseInt(numPlayersOnlineMatcher.group("playersonline"));
                }
            }
            CONTEXT.getLogger().debug(String.format("Gold: %d: Players online: %d", gold, numPlayersOnline));

            if (gold != -1 && numPlayersOnline != -1) {
                // Is this Async enough?
                saveToDatabase(gold, numPlayersOnline);
            }
        });
    }

    private void saveToDatabase(int gold, int numPlayersOnline) {
        // This runs 100% async to not lag the game.
        CONTEXT.getDBsessionFactory().inTransaction(
                session -> {
                    session.persist(new GameSnapshotEnt(LocalDateTime.now(), numPlayersOnline, gold));
                }
        );
    }
}