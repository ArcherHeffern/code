package frog.dptb.client.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@AllArgsConstructor
public class DPTBContext {
    private String modId;
    private Optional<RunAttemptEntity.RunAttemptEntityBuilder> runAttemptEntityBuilder;
    private Optional<RouteAttempt> currentRoute;
    private LocalDateTime lastButtonPress;
    private Logger logger;
    private SessionFactory sessionFactory;
}
