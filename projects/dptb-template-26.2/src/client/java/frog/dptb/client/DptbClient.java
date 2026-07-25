package frog.dptb.client;

import com.mojang.authlib.GameProfile;
import frog.dptb.client.database.DPTBDatabase;
import frog.dptb.client.database.RouteAttempt;
import frog.dptb.client.database.RunAttempt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import static frog.dptb.Dptb.MOD_ID;

public class DptbClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final DPTBDatabase DATABASE = new DPTBDatabase(
			new ArrayList<>(),
			new ArrayList<>(),
			new ArrayList<>()
	);

	public static Optional<RunAttempt> currentRun = Optional.empty();
	public static Optional<RouteAttempt> currentRoute = Optional.empty();

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		LOGGER.info("Initializing Client");
		ClientReceiveMessageEvents.CHAT.register(new ChatMessageListener());
		ClientReceiveMessageEvents.GAME.register(new GameMessageListener());

	}
}

class Utils {
	public static String stringish(@Nullable String s) {
		return s == null ? "(null)": s;
	}
}

class GameMessageListener implements ClientReceiveMessageEvents.Game {

	private static final Pattern BUTTON_PRESSED_REGEX = Pattern.compile(
            "^\\* ➜ The BUTTON was pressed by (?<level>\\[[IVX-]*]) (?<username>[^!]+)!$"
	);
	private static final Pattern COMPLETION_STREAK_REGEX = Pattern.compile(
			"^\\* \\[!] You are now on a (?<streak>\\d+) Completion Streak!$"
	);
	private static final Pattern GOLD_AND_XP_FROM_COMPLETION_REGEX = Pattern.compile(
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

	@Override
	public void onReceiveGameMessage(Component message, boolean overlay) {
		Logger logger = DptbClient.LOGGER;

		String msg = message.getString();

		Matcher buttonPressedMatcher = BUTTON_PRESSED_REGEX.matcher(msg);
		Matcher completionStreakMatcher = COMPLETION_STREAK_REGEX.matcher(msg);
		Matcher goldAndXpFromCompletionMatcher = GOLD_AND_XP_FROM_COMPLETION_REGEX.matcher(msg);
		Matcher convertedGoldFromCompletionMatcher = CONVERTED_GOLD_FROM_COMPLETION_REGEX.matcher(msg);
		Matcher totalBountyFromCompletionMatcher = TOTAL_BOUNTY_FROM_COMPLETION_REGEX.matcher(msg);
		Matcher runStartedMatcher = RUN_STARTED_REGEX.matcher(msg);
		Matcher bountyFromFailure = BOUNTY_FROM_FAILURE_REGEX.matcher(msg);

		boolean buttonPressed = buttonPressedMatcher.matches();
		boolean runStarted = runStartedMatcher.matches();
		boolean completionStreak = completionStreakMatcher.matches();

		// Validate a run completion doesn't happen before a run begins
		if (DptbClient.currentRun.isEmpty()) {
			if (completionStreak) {
			}
		}

		if (buttonPressed) {
			String username = buttonPressedMatcher.group("username");
			String level = buttonPressedMatcher.group("level");
			logger.info("Button pressed by {} with level {}", username, level);
			// TODO: Display time left until button
		} else if (runStarted) {
			DptbClient.currentRun = Optional.of(new RunAttempt(
				Instant.now(),
				Optional.empty(),
			false,
			0,
			false,
			0
			));
		} else if (completionStreak) {
			if (DptbClient.currentRun.isEmpty()) {
				logger.error("Found run completion event before run started");
			} else {
				String strStreak = completionStreakMatcher.group("streak");
				int streak = Integer.parseInt(strStreak);
				DptbClient.currentRun.get().completionStreak(streak);
			}

		}

	}
}

class ChatMessageListener implements ClientReceiveMessageEvents.Chat {

	private boolean isFromGod(GameProfile sender) {
		return false;
	}

	@Override
	public void onReceiveChatMessage(Component message, @Nullable PlayerChatMessage playerChatMessage, @Nullable GameProfile sender, ChatType.Bound boundChatType, Instant timeStamp) {
		if (playerChatMessage != null) {
			DptbClient.LOGGER.info(Utils.stringish(sender != null ? sender.name(): "(null)") + " " + sender.id() + " " + playerChatMessage.toString());
		} else {
			DptbClient.LOGGER.info("PlayerChatMessage was null");
		}
	}
}