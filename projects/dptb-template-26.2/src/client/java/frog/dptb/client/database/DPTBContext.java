package frog.dptb.client.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

import static frog.dptb.Dptb.MOD_ID;

@Data
@AllArgsConstructor
public class DPTBContext {
    private Optional<RunAttemptEntity.RunAttemptEntityBuilder> runAttemptEntityBuilder;
    private Optional<RouteAttempt> currentRoute;
    private Instant lastButtonPress;
    private Logger logger;
    private SessionFactory sessionFactory;

    public static DPTBContext get() {
        return new DPTBContext(
                Optional.empty(),
                Optional.empty(),
                Instant.MIN,
                LoggerFactory.getLogger(MOD_ID),
                DatabaseManager.initialize().get()
        );
    }


}
