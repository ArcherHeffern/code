package frog.dptb.client.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;

import java.util.Optional;

@Data
@AllArgsConstructor
public class DPTBContext {
    private String modId;
    private Logger logger;
    private SessionFactory DBsessionFactory;
    private Optional<DPTBSession> session;
    private int tickCount;

}
