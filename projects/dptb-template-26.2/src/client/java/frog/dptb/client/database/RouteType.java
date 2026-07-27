package frog.dptb.client.database;

import frog.dptb.client.context.DPTBContext;

import java.util.HashMap;
import java.util.Map;

// WARNING: This Enum is used in the database as Ordinal Type. Only add elements to the end.
public enum RouteType {
    NONE("None"),
    UNKNOWN("Unknown"),
    UP_AND_BACK("Up & Back"),
    WRONG_WAY("TODO2"),
    STARGAZER("TODO3"),
    OVERLOADED("TODO4"),
    MR_WOLF("TODO5");

    private final String incomingName;

    // Cache the lookups in a static map for O(1) high-speed conversion
    private static final Map<String, RouteType> BY_NAME = new HashMap<>();

    static {
        for (RouteType modifier : values()) {
            BY_NAME.put(modifier.incomingName.toLowerCase(), modifier);
        }
    }

    RouteType(String incomingName) {
        this.incomingName = incomingName;
    }

    /**
     * Converts your custom text string into the matching Enum constant.
     */
    public static RouteType fromString(String text, DPTBContext context) {
        if (text == null) return null;

        RouteType modifier = BY_NAME.get(text.toLowerCase().trim());
        if (modifier == null) {
            context.getLogger().error("No RouteType mapped for string: " + text);
            return RouteType.UNKNOWN;
        }
        return modifier;
    }
}
