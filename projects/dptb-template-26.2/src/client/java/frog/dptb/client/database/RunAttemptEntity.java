package frog.dptb.client.database;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "RunAttemptEntity")
@NoArgsConstructor // Generates a blank constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Required by @Builder
@Builder
public class RunAttemptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull private LocalDateTime begin;
    @NonNull private LocalDateTime end; // Use this field to validate proper usage
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

    public int getRevenue() {
        return goldFromCompletionStreak + goldFromFullConversion + totalBountyFromCompletion + goldFromPartialConversion;
    }
}
