package frog.dptb.client.database;

import java.time.Instant;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a blank constructor
@AllArgsConstructor // Generates a constructor for all fields
public class RunAttempt {
    private Instant begin;
    private Optional<Instant> end; // Use this field to validate proper usage
    private boolean completed;

    // Success Data
    private boolean earnedRoutebox;
    private int completionStreak;
    private int goldFromCompletionStreak;
    private int xpFromCompletionStreak;
    private int goldFromFullConversion;
    private int totalBountyFromCompletion;

    // Failure Data
    private int goldFromPartialConversion;
}
