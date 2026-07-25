package frog.dptb.client.database;

import java.util.ArrayList;

public record DPTBDatabase(
        ArrayList<RunAttempt> runCompletions,
        ArrayList<PlayerSnapshot> onlineSnapshots,
        ArrayList<RouteAttempt> routeAttempts
) {}
