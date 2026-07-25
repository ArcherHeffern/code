package frog.dptb.client.database;

import java.time.Instant;
import java.util.Optional;

public class RunAttempt {
    public RunAttempt(
            Instant begin,
            Optional<Instant> end, // Use this field to validate proper usage
            boolean completed,
            int payoff,
            boolean earnedRoutebox,
            int completionStreak
    ) {
    }
}
