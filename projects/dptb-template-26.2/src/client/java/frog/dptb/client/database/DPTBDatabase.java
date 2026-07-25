package frog.dptb.client.database;

import java.util.ArrayList;

public record DPTBDatabase(
        ArrayList<RunAttempt> runAttempts,
        ArrayList<PlayerSnapshot> onlineSnapshots,
        ArrayList<RouteAttempt> routeAttempts,
        ArrayList<Session> sessions
) {}
