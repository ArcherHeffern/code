package frog.dptb.client.listeners;

import frog.dptb.client.context.DPTBContext;
import frog.dptb.client.context.DPTBContextProvider;
import frog.dptb.client.context.DPTBSession;
import frog.dptb.client.database.*;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameMessageListener implements ClientReceiveMessageEvents.Game {

    final private static DPTBContext CONTEXT = DPTBContextProvider.get();

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
            "^\\* RARE DROP! You received a Routebox.*$"
    );
    private static final Pattern AFK_REGEX = Pattern.compile(
            "^\\*   AFK! You have been AFK for 60s. Use /spawn  to get back!$"
    );
    private static final Pattern UN_AFK_REGEX = Pattern.compile(
            "^\\* You have been sent back to spawn!$"
    );
    private static final Pattern JOINED_DPTB_REGEX = Pattern.compile(
            "(Sending you to DON'T PRESS THE BUTTON)|(Attempting to teleport you to \\[MVP\\+] Cyborg023's house\\.\\.\\.)"
    );

    @Override
    public void onReceiveGameMessage(Component message, boolean overlay) {
        Logger logger = CONTEXT.getLogger();

        String msg = message.getString();

        Matcher buttonPressedMatcher = BUTTON_PRESSED_REGEX.matcher(msg);
        Matcher completionStreakMatcher = COMPLETION_STREAK_REGEX.matcher(msg);
        Matcher goldAndXpFromCompletionStreakMatcher = GOLD_AND_XP_FROM_COMPLETION_STREAK_REGEX.matcher(msg);
        Matcher convertedGoldFromCompletionMatcher = CONVERTED_GOLD_FROM_COMPLETION_REGEX.matcher(msg);
        Matcher totalBountyFromCompletionMatcher = TOTAL_BOUNTY_FROM_COMPLETION_REGEX.matcher(msg);
        Matcher runStartedMatcher = RUN_STARTED_REGEX.matcher(msg);
        Matcher bountyFromFailureMatcher = BOUNTY_FROM_FAILURE_REGEX.matcher(msg);
        Matcher earnedRouteboxMatcher = EARNED_ROUTEBOX_REGEX.matcher(msg);
        Matcher afkMatcher = AFK_REGEX.matcher(msg);
        Matcher unafkMatcher = UN_AFK_REGEX.matcher(msg);
        Matcher joinedDPTBMatcher = JOINED_DPTB_REGEX.matcher(msg);

        boolean buttonPressed = buttonPressedMatcher.matches();
        boolean runStarted = runStartedMatcher.matches();
        boolean completionStreak = completionStreakMatcher.matches();
        boolean goldAndXpFromCompletionStreak = goldAndXpFromCompletionStreakMatcher.matches();
        boolean convertedGoldFromCompletion = convertedGoldFromCompletionMatcher.matches();
        boolean totalBountyFromCompletion = totalBountyFromCompletionMatcher.matches();
        boolean bountyFromFailure = bountyFromFailureMatcher.matches();
        boolean earnedRoutebox = earnedRouteboxMatcher.matches();
        boolean afk = afkMatcher.matches();
        boolean unafk = unafkMatcher.matches();
        boolean joinedDPTB = joinedDPTBMatcher.matches();

        if (joinedDPTB && CONTEXT.getSession().isEmpty()) {
            // Detect in housing with no session
            logger.debug("Initializing first session");
            DPTBSession s = DPTBSession.initialize();
            CONTEXT.setSession(Optional.of(s));
            return;
        }

        if (CONTEXT.getSession().isEmpty()) {
            return;
        }
        DPTBSession session = CONTEXT.getSession().get();

        if (buttonPressed) {
            String username = buttonPressedMatcher.group("username");
            String level = buttonPressedMatcher.group("level");
            logger.info("Button pressed by {} with level {}", username, level);
            session.setLastButtonPress(LocalDateTime.now());
        } else if (runStarted) {
            logger.debug("[Run Started]");
            Optional<RunAttemptEnt.RunAttemptEntBuilder> runAttemptBuilder = Optional.of(RunAttemptEnt.builder().begin(LocalDateTime.now()));
            session.setRunAttemptEntityBuilder(runAttemptBuilder);
        } else if (afk) {
            logger.debug("User is AFK. Session has been paused");
            session.pause(CONTEXT.getDBsessionFactory(), true);
        } else if (unafk) {
            logger.debug("User is no longer AFK. Session has been resumed.");
            session.resume();
        } else if (joinedDPTB) {
            // Detect joining housing and have previous session
            logger.debug("Joined Housing and have previous session");
            session.resume();
        }


        Optional<RouteAttemptEnt> currentRoute = session.getCurrentRoute();

        // After this point - All messages pertain to if a run has already started
        if (session.getRunAttemptEntityBuilder().isEmpty()) {
            return;
        }
        RunAttemptEnt.RunAttemptEntBuilder runBuilder = session.getRunAttemptEntityBuilder().get();


        if (completionStreak) {
            logger.debug("[Completion Streak]");
            String strStreak = completionStreakMatcher.group("streak");
            int streak = Integer.parseInt(strStreak);
            runBuilder.completionStreak(streak);
        } else if (goldAndXpFromCompletionStreak) {
            logger.debug("[Completion Streak Gold/XP]");
            int gold = Integer.parseInt(goldAndXpFromCompletionStreakMatcher.group("gold"));
            int xp = Integer.parseInt(goldAndXpFromCompletionStreakMatcher.group("xp"));
            runBuilder.goldFromCompletionStreak(gold);
            runBuilder.xpFromCompletionStreak(xp);
        } else if (convertedGoldFromCompletion) {
            logger.debug("[Converted Gold From Completion]");
            int gold = Integer.parseInt(convertedGoldFromCompletionMatcher.group("gold"));
            runBuilder.goldFromFullConversion(gold);
        } else if (earnedRoutebox) {
            logger.debug("[Earned Routebox]");
            runBuilder.earnedRoutebox(true);
        } else if (totalBountyFromCompletion) {
            logger.debug("[Total Bounty From Completion]");
            int gold = Integer.parseInt(totalBountyFromCompletionMatcher.group("gold"));
            runBuilder.totalBountyFromCompletion(gold);
            runBuilder.end(LocalDateTime.now());
            runBuilder.completed(true);

            logger.debug("=== Completed Run! ===");
            RunAttemptEnt run = runBuilder.build();
            logger.debug("Run Revenue: " + run.getRevenue());
            DatabaseManager.persist(CONTEXT.getDBsessionFactory(), run);
            session.setTotalRevenue(session.getTotalRevenue() + run.getRevenue());
            session.setRunAttemptEntityBuilder(Optional.empty());
        } else if (bountyFromFailure) {
            logger.debug("[Bounty From Failure]");
            int gold = Integer.parseInt(bountyFromFailureMatcher.group("gold"));
            runBuilder.goldFromPartialConversion(gold);
            runBuilder.end(LocalDateTime.now());

            logger.debug("=== Failed Run. ===");
            logger.debug(runBuilder.toString());
            RunAttemptEnt run = runBuilder.build();
            session.setTotalRevenue(session.getTotalRevenue() + run.getRevenue());
            DatabaseManager.persist(CONTEXT.getDBsessionFactory(), run);
            session.setRunAttemptEntityBuilder(Optional.empty());
        }

    }
}
