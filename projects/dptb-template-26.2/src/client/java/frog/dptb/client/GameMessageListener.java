package frog.dptb.client;

import frog.dptb.client.database.RouteAttempt;
import frog.dptb.client.database.RunAttempt;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameMessageListener implements ClientReceiveMessageEvents.Game {

    private static final Pattern BUTTON_PRESSED_REGEX = Pattern.compile(
            "^\\* ➜ The BUTTON was pressed by (?<level>\\[[IVX-]*]) (?<username>[^!]+)!$"
    );
    private static final Pattern COMPLETION_STREAK_REGEX = Pattern.compile(
            "^\\* \\[!] You are now on a (?<streak>\\d+) Completion Streak!$"
    );
    private static final Pattern GOLD_AND_XP_FROM_COMPLETION_STREAK_REGEX = Pattern.compile(
            "^\\* (?<gold>\\d+)⛂ Gold & (?<xp>\\d+)xp from that Completion Streak!$"
    );
    private static final Pattern CONVERTED_GOLD_FROM_COMPLETION_REGEX = Pattern.compile(
            "^\\* Successfully converted (?<gold>\\d+)⛂ gold into stat form!$"
    );
    private static final Pattern TOTAL_BOUNTY_FROM_COMPLETION_REGEX = Pattern.compile(
            "^\\* Total from Bounty: (?<gold>\\d+)⛂ Gold$"
    );
    private static final Pattern RUN_STARTED_REGEX = Pattern.compile(
            "^\\* Run started!$"
    );
    private static final Pattern BOUNTY_FROM_FAILURE_REGEX = Pattern.compile(
            "^\\* You earned (?<gold>\\d+)⛂ from your bounty!$"
    );
    private static final Pattern EARNED_ROUTEBOX_REGEX = Pattern.compile(
            "TODO"
    );

    @Override
    public void onReceiveGameMessage(Component message, boolean overlay) {
        Logger logger = DptbClient.LOGGER;
        Optional<RunAttempt> currentRun = DptbClient.currentRun;
        Optional<RouteAttempt> currentRoute = DptbClient.currentRoute;

        String msg = message.getString();

        Matcher buttonPressedMatcher = BUTTON_PRESSED_REGEX.matcher(msg);
        Matcher completionStreakMatcher = COMPLETION_STREAK_REGEX.matcher(msg);
        Matcher goldAndXpFromCompletionStreakMatcher = GOLD_AND_XP_FROM_COMPLETION_STREAK_REGEX.matcher(msg);
        Matcher convertedGoldFromCompletionMatcher = CONVERTED_GOLD_FROM_COMPLETION_REGEX.matcher(msg);
        Matcher totalBountyFromCompletionMatcher = TOTAL_BOUNTY_FROM_COMPLETION_REGEX.matcher(msg);
        Matcher runStartedMatcher = RUN_STARTED_REGEX.matcher(msg);
        Matcher bountyFromFailureMatcher = BOUNTY_FROM_FAILURE_REGEX.matcher(msg);
        Matcher earnedRouteboxMatcher = EARNED_ROUTEBOX_REGEX.matcher(msg);

        boolean buttonPressed = buttonPressedMatcher.matches();
        boolean runStarted = runStartedMatcher.matches();
        boolean completionStreak = completionStreakMatcher.matches();
        boolean goldAndXpFromCompletionStreak = goldAndXpFromCompletionStreakMatcher.matches();
        boolean convertedGoldFromCompletion = convertedGoldFromCompletionMatcher.matches();
        boolean totalBountyFromCompletion = totalBountyFromCompletionMatcher.matches();
        boolean bountyFromFailure = bountyFromFailureMatcher.matches();
        boolean earnedRoutebox = earnedRouteboxMatcher.matches();

        if (buttonPressed) {
            String username = buttonPressedMatcher.group("username");
            String level = buttonPressedMatcher.group("level");
            logger.info("Button pressed by {} with level {}", username, level);
            // TODO: Display time left until button
        } else if (runStarted) {
            logger.debug("[Run Started]");
            DptbClient.currentRun = Optional.of(new RunAttempt(
                    Instant.now(),
                    Optional.empty(),
                    false,
                    false,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            ));
        } else if (completionStreak) {
            logger.debug("[Completion Streak]");
            if (currentRun.isEmpty()) {
                logger.error("Found [Completion Streak] event before run started");
                return;
            }
            String strStreak = completionStreakMatcher.group("streak");
            int streak = Integer.parseInt(strStreak);
            currentRun.get().setCompletionStreak(streak);
        } else if (goldAndXpFromCompletionStreak) {
            logger.debug("[Completion Streak Gold/XP]");
            if (currentRun.isEmpty()) {
                logger.error("Found [Completion Streak Gold/XP] event before run started");
                return;
            }
            int gold = Integer.parseInt(goldAndXpFromCompletionStreakMatcher.group("gold"));
            int xp = Integer.parseInt(goldAndXpFromCompletionStreakMatcher.group("xp"));
            currentRun.get().setGoldFromCompletionStreak(gold);
            currentRun.get().setXpFromCompletionStreak(xp);
        } else if (convertedGoldFromCompletion) {
            logger.debug("[Converted Gold From Completion]");
            if (currentRun.isEmpty()) {
                logger.error("Found [Converted Gold From Completion] event before run started");
                return;
            }
            int gold = Integer.parseInt(convertedGoldFromCompletionMatcher.group("gold"));
            currentRun.get().setGoldFromFullConversion(gold);
        } else if (totalBountyFromCompletion) {
            logger.debug("[Total Bounty From Completion]");
            if (currentRun.isEmpty()) {
                logger.error("Found [Total Bounty From Completion] event before run started");
                return;
            }
            int gold = Integer.parseInt(totalBountyFromCompletionMatcher.group("gold"));
            currentRun.get().setTotalBountyFromCompletion(gold);
            currentRun.get().setEnd(Optional.of(Instant.now()));
            currentRun.get().setCompleted(true);

            logger.debug("=== Completed Run! ===");
            logger.debug(currentRun.get().toString());
            DptbClient.DATABASE.runAttempts().add(currentRun.get());
            DptbClient.currentRun = Optional.empty();
        } else if (bountyFromFailure) {
            logger.debug("[Bounty From Failure]");
            if (currentRun.isEmpty()) {
                logger.error("Found [Bounty From Failure] event before run started");
                return;
            }
            int gold = Integer.parseInt(bountyFromFailureMatcher.group("gold"));
            currentRun.get().setGoldFromPartialConversion(gold);
            currentRun.get().setEnd(Optional.of(Instant.now()));

            logger.debug("=== Failed Run. ===");
            logger.debug(currentRun.get().toString());
            DptbClient.DATABASE.runAttempts().add(currentRun.get());
            DptbClient.currentRun = Optional.empty();
        } else if (earnedRoutebox) {
            logger.debug("[Earned Routebox]");
            if (currentRun.isEmpty()) {
                logger.error("Found [Earned Routebox] event before run started");
                return;
            }
            currentRun.get().setEarnedRoutebox(true);
        }

    }
}
