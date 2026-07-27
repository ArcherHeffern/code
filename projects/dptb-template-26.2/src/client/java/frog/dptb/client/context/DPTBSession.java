package frog.dptb.client.context;

import frog.dptb.client.database.DatabaseManager;
import frog.dptb.client.database.RunAttemptEnt;
import frog.dptb.client.database.SessionEnt;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.SessionFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Data
@AllArgsConstructor
public class DPTBSession {
    private Optional<RunAttemptEnt.RunAttemptEntBuilder> runAttemptEntityBuilder;
    private LocalDateTime lastButtonPress;
    private int totalRevenue;
    private LocalDateTime sessionStart;
    private ArrayList<Duration> afkPeriods;

    // This field is a HACK to avoid calling .build() on RunAttemptEntBuilder too early
    // We need to know if we're running a route or a run.
    private boolean isRunningRoute;

    // While this class recognises AFK periods, the database will make separate sessions to exclude AFK periods
    // These two fields are mutually exclusive.
    private Optional<LocalDateTime> dbSessionStart;
    private Optional<LocalDateTime> awayFromHousingSince;

    public Duration getSessionDuration() {
        LocalDateTime recentTime = awayFromHousingSince.orElse(LocalDateTime.now());
        Duration d = Duration.between(this.getSessionStart(), recentTime);
        for (Duration afkPeriod: this.getAfkPeriods()) {
            d = d.minus(afkPeriod);
        }
        return d;
    }

    public static DPTBSession initialize() {
        return new DPTBSession(
                Optional.empty(),
                LocalDateTime.MIN,
                0,
                LocalDateTime.now(),
                new ArrayList<>(),
                false,
                Optional.of(LocalDateTime.now()),
                Optional.empty()
        );
    }

    public void resume() {
        getAwayFromHousingSince().ifPresentOrElse((t) -> {
            Duration b = Duration.between(t, LocalDateTime.now());
            getAfkPeriods().add(b);
            setAwayFromHousingSince(Optional.empty());
            setDbSessionStart(Optional.of(LocalDateTime.now()));
        }, () -> {
            throw new Error("Cannot resume a session that hasn't been paused.");
        });
    }

    public void pause(SessionFactory sessionFactory, boolean isFromAFK) {
        getDbSessionStart().ifPresentOrElse(
                (sessionStart) -> {
                    LocalDateTime t = LocalDateTime.now();
                    if (isFromAFK) {
                        t = t.minus(Duration.ofMinutes(1));
                    }
                    SessionEnt sessionEnt = new SessionEnt(sessionStart, t);
                    DatabaseManager.persist(sessionFactory, sessionEnt);
                    setDbSessionStart(Optional.empty());
                    setAwayFromHousingSince(Optional.of(LocalDateTime.now().minus(Duration.ofMinutes(1))));
                },
                () -> {
                    throw new Error("Cannot pause session that wasn't started.");
                }
        );
    }
}
