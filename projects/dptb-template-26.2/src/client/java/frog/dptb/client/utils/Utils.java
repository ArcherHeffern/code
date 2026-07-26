package frog.dptb.client.utils;

import org.jetbrains.annotations.Nullable;

public class Utils {
    public static String stringish(@Nullable String s) {
        return s == null ? "(null)" : s;
    }
}
