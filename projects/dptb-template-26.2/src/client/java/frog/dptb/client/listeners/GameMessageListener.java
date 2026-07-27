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
            "^\\* (?<gold>[0-9,]+)⛂ Gold & (?<xp>[0-9,]+)xp from that Completion Streak!$"
    );
    private static final Pattern CONVERTED_GOLD_FROM_COMPLETION_REGEX = Pattern.compile(
            "^\\* Successfully converted (?<gold>[0-9,]+)⛂ gold into stat form!$"
    );
    private static final Pattern TOTAL_BOUNTY_FROM_COMPLETION_REGEX = Pattern.compile(
            "^\\* Total from Bounty: (?<gold>[0-9,]+)⛂ Gold$"
    );
    private static final Pattern RUN_STARTED_REGEX = Pattern.compile(
            "^\\* Run started!$"
    );
    private static final Pattern BOUNTY_FROM_FAILURE_REGEX = Pattern.compile(
            "^\\* You earned (?<gold>[0-9,]+)⛂ from your bounty!$"
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
    private static final Pattern ROUTE_MODIFIER_SELECTED_REGEX = Pattern.compile(
            "^\\* ROUTE! You selected (?<modifier>.*) modifier!$"
    );
    private static final Pattern ROUTE_FAILED_REGEX = Pattern.compile(
            "^(\\* OH NO! You failed to complete your Route!)|(\\* OH NO! You died and failed your selected Route!)$"
    );
    private static final Pattern ROUTE_STARTED_REGEX = Pattern.compile(
            "^\\* ROUTE! You have started the Route (?<route>.*)$"
    );
    private static final Pattern UP_AND_BACK_HALFWAY_REGEX = Pattern.compile(
            "^\\* >> ALMOST THERE! You reached the END, now return to SPAWN to complete your Route!$"
    );
    private static final Pattern COMPLETED_ROUTE_REGEX = Pattern.compile(
            "^\\*   CONGRATS! You completed the (?<route>.*) Route!$"
    );
    private static final Pattern GOLD_EARNED_FROM_ROUTE_REGEX = Pattern.compile(
            "^\\*   Gold Earned: (?<gold>[0-9,]+)⛂$"
    );
    private static final Pattern XP_EARNED_FROM_ROUTE_REGEX = Pattern.compile(
            "^\\*   XP Earned: (?<xp>[0-9,]+) xp$"
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
        Matcher routeModifierSelectedMatcher = ROUTE_MODIFIER_SELECTED_REGEX.matcher(msg);
        Matcher routeFailedMatcher = ROUTE_FAILED_REGEX.matcher(msg);
        Matcher routeStartedMatcher = ROUTE_STARTED_REGEX.matcher(msg);
        Matcher goldEarnedFromRouteMatcher = GOLD_EARNED_FROM_ROUTE_REGEX.matcher(msg);
        Matcher xpEarnedFromRouteMatcher = XP_EARNED_FROM_ROUTE_REGEX.matcher(msg);


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
        boolean routeModifierSelected = routeModifierSelectedMatcher.matches();
        boolean routeFailed = routeFailedMatcher.matches();
        boolean routeStarted = routeStartedMatcher.matches();
        boolean goldEarnedFromRoute = goldEarnedFromRouteMatcher.matches();
        boolean xpEarnedFromRoute = xpEarnedFromRouteMatcher.matches();

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
            // Check if already present since routeModifierSelected might have already created the RunAttemptBuilder
            session.getRunAttemptEntityBuilder().ifPresentOrElse(
                    (runAttemptEntBuilder -> {
                        runAttemptEntBuilder.begin(LocalDateTime.now());
                        logger.debug("[Route Started]");
                    }),
                    () -> {
                        logger.debug("[Run Started]");
                        var runAttemptBuilder = Optional.of(
                                RunAttemptEnt
                                        .builder()
                                        .begin(LocalDateTime.now())
                                        .routeModifier(RouteModifier.NONE)
                                        .routeType(RouteType.NONE)
                        );
                        session.setRunAttemptEntityBuilder(runAttemptBuilder);
                    }
            );
        } else if (afk) {
            logger.debug("User is AFK. Session has been paused");
            session.pause(CONTEXT.getDBsessionFactory(), true);
        } else if (unafk) {
            logger.debug("User is no longer AFK. Session has been resumed.");
            session.resume();
        } else if (joinedDPTB) {
            logger.debug("Joined Housing and have previous session");
            session.resume();
        } else if (routeModifierSelected) {
            String m = routeModifierSelectedMatcher.group("modifier");
            RouteModifier routeModifier = RouteModifier.fromString(m, CONTEXT);
            var runAttemptBuilder = Optional.of(RunAttemptEnt.builder().routeModifier(routeModifier));
            session.setRunAttemptEntityBuilder(runAttemptBuilder);
            session.setRunningRoute(true);
            logger.debug(String.format("Route Modifier Selected: %s", routeModifier.name()));
        }

        // After this point - All messages pertain to if a run has already started
        if (session.getRunAttemptEntityBuilder().isEmpty()) {
            return;
        }
        RunAttemptEnt.RunAttemptEntBuilder runBuilder = session.getRunAttemptEntityBuilder().get();


        if (routeStarted) {
            // This event happens after the `run started` event, so the builder already exists.
            String r = routeStartedMatcher.group("route");
            RouteType routeType = RouteType.fromString(r, CONTEXT);
            runBuilder.routeType(routeType);
            logger.debug(String.format("Started Route: %s", routeType.name()));
        } else if (completionStreak) {
            logger.debug("[Completion Streak]");
            String strStreak = completionStreakMatcher.group("streak");
            int streak = Integer.parseInt(strStreak);
            runBuilder.completionStreak(streak);
        } else if (goldAndXpFromCompletionStreak) {
            logger.debug("[Completion Streak Gold/XP]");
            int gold = Integer.parseInt(goldAndXpFromCompletionStreakMatcher.group("gold").replace(",", ""));
            int xp = Integer.parseInt(goldAndXpFromCompletionStreakMatcher.group("xp").replace(",", ""));
            runBuilder.goldFromCompletionStreak(gold);
            runBuilder.xpFromCompletionStreak(xp);
        } else if (convertedGoldFromCompletion) {
            logger.debug("[Converted Gold From Completion]");
            int gold = Integer.parseInt(convertedGoldFromCompletionMatcher.group("gold").replace(",", ""));
            runBuilder.goldFromFullConversion(gold);
        } else if (earnedRoutebox) {
            logger.debug("[Earned Routebox]");
            runBuilder.earnedRoutebox(true);
        } else if (totalBountyFromCompletion) {
            logger.debug("[Total Bounty From Completion]");
            int gold = Integer.parseInt(totalBountyFromCompletionMatcher.group("gold").replace(",", ""));
            runBuilder.totalBountyFromCompletion(gold);

            // WARNING: This field exists for this line
            // We cannot call runBuilder.build() since the builder data is not yet complete and builders don't have getters.
            if (!session.isRunningRoute()) {
                logger.debug("=== Completed Run! ===");
                finishRun(true, session, runBuilder);
            }
        } else if (bountyFromFailure) {
            logger.debug("[Bounty From Failure]");
            int gold = Integer.parseInt(bountyFromFailureMatcher.group("gold").replace(",", ""));
            runBuilder.goldFromPartialConversion(gold);

            // WARNING: This field exists for this line
            // We cannot call runBuilder.build() since the builder data is not yet complete and builders don't have getters.
            if (!session.isRunningRoute()) {
                logger.debug("=== Failed Run. ===");
                finishRun(false, session, runBuilder);
            }
        } else if (routeFailed) {
            logger.debug("=== Failed Route. ===");
            finishRun(false, session, runBuilder);
        } else if (goldEarnedFromRoute) {
            int gold = Integer.parseInt(goldEarnedFromRouteMatcher.group("gold").replace(",", ""));
            runBuilder.goldEarnedFromRoute(gold);
            logger.debug(String.format("Earned %d gold from route completion", gold));
        } else if (xpEarnedFromRoute) {
            int xp = Integer.parseInt(xpEarnedFromRouteMatcher.group("xp").replace(",", ""));
            runBuilder.xpEarnedFromRoute(xp);
            logger.debug(String.format("Earned %d xp from route completion", xp));
            finishRun(true, session, runBuilder);
        }
    }

    private void finishRun(boolean success, DPTBSession session, RunAttemptEnt.RunAttemptEntBuilder runBuilder) {
        runBuilder.completed(success);
        runBuilder.end(LocalDateTime.now());
        RunAttemptEnt run = runBuilder.build();
        DatabaseManager.persist(CONTEXT.getDBsessionFactory(), run);
        session.setTotalRevenue(session.getTotalRevenue() + run.getRevenue());
        session.setRunAttemptEntityBuilder(Optional.empty());
        session.setRunningRoute(false);
    }
}
