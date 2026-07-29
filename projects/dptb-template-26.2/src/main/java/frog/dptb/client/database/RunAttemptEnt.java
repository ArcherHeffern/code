package frog.dptb.client.database;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RunAttemptEnt")
@NoArgsConstructor // Generates a blank constructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Required by @Builder
@Builder
@Getter
public class RunAttemptEnt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private LocalDateTime begin;
    @NonNull
    private LocalDateTime end; // Use this field to validate proper usage
    private boolean completed;

    // Success Data
    private boolean earnedRoutebox;
    private int completionStreak;
    private int goldFromCompletionStreak;
    private int xpFromCompletionStreak;
    private int goldFromFullConversion;
    private int totalBountyFromCompletion;

    // Route Data
    private RouteType routeType;
    private RouteModifier routeModifier;
    private int goldEarnedFromRoute;
    private int xpEarnedFromRoute;


    // Failure Data
    private int goldFromPartialConversion;

    public int getRevenue() {
        return this.goldFromCompletionStreak + goldFromFullConversion + totalBountyFromCompletion + goldFromPartialConversion + goldEarnedFromRoute;
    }
}
