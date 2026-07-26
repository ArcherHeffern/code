package frog.dptb.client.context;

import frog.dptb.client.database.DatabaseManager;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

public class DPTBContextProvider {

    private static DPTBContext context = null;

    public static DPTBContext get() {
        if (context == null) {
            String MOD_ID = frog.dptb.Dptb.MOD_ID;
            context = new DPTBContext(
                    MOD_ID,
                    Optional.empty(),
                    Optional.empty(),
                    LocalDateTime.MIN,
                    LoggerFactory.getLogger(MOD_ID),
                    DatabaseManager.initialize().get()
            );
            return context;
        }
        return context;
    }


}
