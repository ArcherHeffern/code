package frog.dptb.client.utils;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static frog.dptb.client.DptbClient.CONTEXT;

public class Utils {
    public static String stringish(@Nullable String s) {
        return s == null ? "(null)" : s;
    }

    public static boolean isHypixel(Minecraft client) {
        if (client.getCurrentServer() == null) {
            return false;
        }
        String ip = client.getCurrentServer().ip.strip().toLowerCase();
        return ip.equals("mc.hypixel.net");
    }

    public static boolean checkIfInDPTB(Minecraft client) {
        if (client.level == null || client.player == null) return false;
        if (!Utils.isHypixel(client)) return false;
        List<String> scoreboard = HypixelSidebarReader.getVisualLines(client);
        if (scoreboard.size() < 4) {
            return false;
        }
        if (!scoreboard.get(1).equals("DON'T PRESS")) {
            return false;
        }
        if (!scoreboard.get(2).equals("THE BUTTON 2")) {
            return false;
        }
        return scoreboard.get(3).equals("by Cyborg023 [LG]");
    }

    public static boolean wasOrIsInDPTB() {
        return CONTEXT.getSession().map(
            s -> s.getDbSessionStart().map(
                start -> {
                    Duration between = Duration.between(start, LocalDateTime.now());
                    return between.compareTo(Duration.ofSeconds(5)) > 0;
                }
        ).orElse(false)).orElse(false);
    }

    // Returns true if left is strictly greater than right
    public static <T extends Comparable<T>> boolean isGreaterThan(T left, T right) {
        return left.compareTo(right) > 0;
    }

    // Returns true if left is strictly less than right
    public static <T extends Comparable<T>> boolean isLessThan(T left, T right) {
        return left.compareTo(right) < 0;
    }

    // MAYBE_NEGATIVE_NUMBER (MAYBE_SPACES SYMBOL MAYBE_NEGATIVE_NUMBER)+
    public static final Pattern MATH_EQUATION_REGEX = Pattern.compile(
            "-?[0-9,]+(\\s*[+x*/-]\\s*-?[0-9,]+)+"
    );

    // Ignores order of operations
    public static float mathSolver(String s) {
        String[] tokens = s.replace(",", "").split("\\s+");
        float result = Float.parseFloat(tokens[0]);
        String op = "";
        boolean negative = false;
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.equals("-") && !op.isEmpty()) {
                negative = true;
            } else if ("+-/*x".contains(token)) {
                op = token;
            } else {
                float number = Float.parseFloat(token);
                if (negative) {
                    number = number * -1;
                }

                switch (op) {
                    case "+" -> result += number;
                    case "-" -> result -= number;
                    case "*", "x" -> result *= number;
                    case "/" -> result /= number;
                }

                negative = false;
                op = "";
            }
        }
        return result;
    }
}
