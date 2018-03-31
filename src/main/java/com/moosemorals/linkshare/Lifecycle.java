package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

@WebListener
public final class Lifecycle implements ServletContextListener {


    private final Logger log = LoggerFactory.getLogger(Backend.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            AuthManager.getInstance().loadDatabase();
        } catch (IOException ex) {
            log.error("Can't load auth database", ex);
        }
        LinkManager.getInstance().loadLinks();
        EventPlexer.getInstance().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            AuthManager.getInstance().saveDatabase();
        } catch (IOException ex) {
            log.error("Can't save auth database", ex);
        }
        EventPlexer.getInstance().stop();
        LinkManager.getInstance().saveLinks();
    }


}

