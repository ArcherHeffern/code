package frog.dptb.client;

import org.jetbrains.annotations.Nullable;

class Utils {
    public static String stringish(@Nullable String s) {
        return s == null ? "(null)" : s;
    }
}
