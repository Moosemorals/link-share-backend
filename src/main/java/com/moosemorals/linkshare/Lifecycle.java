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


    private final Logger log = LoggerFactory.getLogger(Backend.class);

    private Properties props = new Properties();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            props.load(getClass().getResourceAsStream("/default.properties"));
            LinkManager.getInstance().loadLinks(props);
            AuthManager.getInstance().loadDatabase(props);
            EventPlexer.getInstance().start();
        } catch (IOException ex) {
            log.error("Startup error", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EventPlexer.getInstance().stop();
        try {
            AuthManager.getInstance().saveDatabase(props);
            LinkManager.getInstance().saveLinks(props);
        } catch (IOException ex) {
            log.error("Shutdown error", ex);
        }
    }
}

