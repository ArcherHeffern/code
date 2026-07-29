package frog.dptb.client.database;

import frog.dptb.client.context.DPTBContext;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

// WARNING: This Enum is used in the database as Ordinal Type. Only add elements to the end.
@Getter
public enum RouteModifier {
    NONE("None"),
    UNKNOWN("Unknown"),
    CANT_TOUCH_THIS("Can't Touch This"),
    EMPTY_POCKETS("Empty Pockets"),
    FRAGILE("Fragile"),
    BLIND_SNAIL("Blind Snail"),
    SCARLET_HUNT("Scarlet Hunt"),
    EARTHQUAKE("Earthquake"),
    WALL_HUGGER("Wallhugger"),
    ECHO_LOCATION("Echo Location");

    private final String incomingName;

    // Cache the lookups in a static map for O(1) high-speed conversion
    private static final Map<String, RouteModifier> BY_NAME = new HashMap<>();

    static {
        for (RouteModifier modifier : values()) {
            BY_NAME.put(modifier.incomingName.toLowerCase(), modifier);
        }
    }

    RouteModifier(String incomingName) {
        this.incomingName = incomingName;
    }

    /**
     * Converts your custom text string into the matching Enum constant.
     */
    public static RouteModifier fromString(String text, DPTBContext context) {
        if (text == null) return null;

        RouteModifier modifier = BY_NAME.get(text.toLowerCase().trim());
        if (modifier == null) {
            context.getLogger().error("No RouteModifier mapped for string: " + text);
            return RouteModifier.UNKNOWN;
        }
        return modifier;
    }
}
