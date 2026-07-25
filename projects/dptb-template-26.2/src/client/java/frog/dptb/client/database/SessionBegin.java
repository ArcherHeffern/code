package frog.dptb.client.database;

import java.time.Instant;

public record SessionBegin(
        Instant timestamp
) {
}
