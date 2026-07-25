package frog.dptb.client.database;

import java.time.Duration;
import java.time.Instant;

public record RouteAttempt (
    Instant begin,
    Instant end,
    boolean completed,
    RouteType routeType,
    int playersOnline,
    Duration timeSpent,
    float revenue
) {}
