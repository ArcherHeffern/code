package frog.dptb.client.database;

import java.time.Instant;

public record PlayerSnapshot(
    Instant time,
    int numPlayers
) {}
