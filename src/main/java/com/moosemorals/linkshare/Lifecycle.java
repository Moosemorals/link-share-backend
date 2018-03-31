package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.Properties;

@WebListener
public final class Lifecycle implements ServletContextListener {
    private static final long PERIOD = 15 * 60 * 997;   // Fifteen minutes, ish
    private final Logger log = LoggerFactory.getLogger(Backend.class);
    private Properties props = new Properties();

    private Thread periodicallyThread;
    private final Runnable periodically = new Runnable() {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(PERIOD);
                } catch (InterruptedException e) {
                    return;
                }
                try {
                    LinkManager.getInstance().saveLinks(props);
                    AuthManager.getInstance().saveDatabase(props);
                } catch (IOException e) {
                    log.error("Problem with periodic save");
                }
            }
        }
    };

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            props.load(getClass().getResourceAsStream("/default.properties"));
            LinkManager.getInstance().loadLinks(props);
            AuthManager.getInstance().loadDatabase(props);
            EventPlexer.getInstance().start();

            periodicallyThread = new Thread(periodically, "Periodically");
            periodicallyThread.start();
        } catch (IOException ex) {
            log.error("Startup error", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        periodicallyThread.interrupt();
        EventPlexer.getInstance().stop();
        try {
            AuthManager.getInstance().saveDatabase(props);
            LinkManager.getInstance().saveLinks(props);
        } catch (IOException ex) {
            log.error("Shutdown error", ex);
        }
    }
}

