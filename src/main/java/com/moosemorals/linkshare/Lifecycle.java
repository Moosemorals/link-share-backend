package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.*;

@WebListener
public final class Lifecycle implements ServletContextListener {


    private final File linkFile = new File("/home/osric/tmp/links.json");
    private final Logger log = LoggerFactory.getLogger(Backend.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        loadLinks();
        EventPlexer.getInstance().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EventPlexer.getInstance().stop();
        saveLinks();
    }

    private void saveLinks() {

        JsonArray links = LinkManager.getInstance().getJsonLinks(0);

        log.debug("Saving {} link(s)", links.size());

        try (FileWriter out = new FileWriter(linkFile)) {
            out.write(links.toString());
            out.flush();
        } catch (IOException ex) {
            log.error("Can't save links to {}", linkFile.getAbsoluteFile(), ex);
        }
    }

    private void loadLinks() {

        try (JsonReader in = Json.createReader(new FileReader(linkFile))) {
            JsonArray links = in.readArray();

            LinkManager.getInstance().setLinks(links);

        } catch (IOException ex) {
            log.warn("Can't load links from {}", linkFile, ex);
        }


    }

}

