package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public final class Lifecycle implements ServletContextListener{

    private final Logger log = LoggerFactory.getLogger(Backend.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EventPlexer.getInstance().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EventPlexer.getInstance().stop();
    }
}
