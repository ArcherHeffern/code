package frog.dptb.client.utils;

import frog.dptb.client.HypixelSidebarReader;
import frog.dptb.client.context.DPTBContext;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

}
