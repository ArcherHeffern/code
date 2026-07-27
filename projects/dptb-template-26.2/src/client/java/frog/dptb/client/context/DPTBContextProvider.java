package frog.dptb.client.context;

import frog.dptb.client.database.DatabaseManager;
import org.hibernate.SessionFactory;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DPTBContextProvider {

    private static DPTBContext context = null;

    public static DPTBContext get() {
        if (context == null) {
            String MOD_ID = frog.dptb.Dptb.MOD_ID;
            Optional<SessionFactory> maybeSessionFactory = DatabaseManager.initialize();
            maybeSessionFactory.ifPresentOrElse(sessionFactory -> {
                context = new DPTBContext(
                        MOD_ID,
                        LoggerFactory.getLogger(MOD_ID),
                        sessionFactory,
                        Optional.empty(),
                        0
                );
            }, () -> {
                throw new Error("Failed to initialize sessionFactory in DPTBContextProvider");
            });
            return context;
        }
        return context;
    }


}
