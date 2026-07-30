package frog.dptb.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class HypixelSidebarReader {

    /**
     * Extracts and returns the plain-text lines of Hypixel's right-hand sidebar.
     * Sorted in precise top-to-bottom visual order.
     */
    public static List<String> getVisualLines(Minecraft client) {
        List<String> orderedLines = new ArrayList<>();
        // 1. Safety check to make sure the client is connected to a level
        if (client.level == null) return orderedLines;

        Scoreboard scoreboard = client.level.getScoreboard();

        // 2. Isolate the objective bound to the right-side panel
        Objective sidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebarObjective == null) return orderedLines;

        // 3. Request only the score rows assigned to this sidebar objective
        Collection<PlayerScoreEntry> scores = scoreboard.listPlayerScores(sidebarObjective);
        if (scores == null || scores.isEmpty()) return orderedLines;

        // 4. Convert to a list and sort by score value descending.
        // Minecraft renders the highest internal values at the top of the sidebar.
        List<PlayerScoreEntry> sortedScores = new ArrayList<>(scores);
        sortedScores.sort(Comparator.comparingInt(PlayerScoreEntry::value).reversed());

        // 5. Parse each row's underlying team metadata
        for (PlayerScoreEntry entry : sortedScores) {
            String fakePlayerKey = entry.ownerName().getString(); // E.g., "§f" or a spacing code
//
//            // Hypixel stores line text inside the PlayerTeam assigned to this fake name
            PlayerTeam team = scoreboard.getPlayersTeam(fakePlayerKey);

            String plainLineText;
            if (team != null) {
                Component fullRowComponent = team.getPlayerPrefix().copy()
                        .append(team.getPlayerSuffix());

                plainLineText = fullRowComponent.getString();
            } else {
                plainLineText = fakePlayerKey;
            }
            plainLineText = plainLineText.replaceAll("§[0-9a-fk-orxX]", "");

            // Convert to plain text string (automatically drops color characters like §)
            plainLineText = plainLineText.trim();

            // Skip completely empty spacer rows
            if (!plainLineText.isEmpty()) {
                orderedLines.add(plainLineText);
            }
        }

        return orderedLines;
    }
}