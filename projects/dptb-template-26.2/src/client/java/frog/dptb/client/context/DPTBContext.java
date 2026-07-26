package frog.dptb.client.context;

import frog.dptb.client.database.RouteAttemptEnt;
import frog.dptb.client.database.RunAttemptEnt;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@AllArgsConstructor
public class DPTBContext {
    private String modId;
    private Optional<RunAttemptEnt.RunAttemptEntBuilder> runAttemptEntityBuilder;
    private Optional<RouteAttemptEnt> currentRoute;
    private LocalDateTime lastButtonPress;
    private Logger logger;
    private SessionFactory sessionFactory;
}
