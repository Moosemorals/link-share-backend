package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

final class LinkManager {

    private static final String FILENAME = "links.json";
    private static final LinkManager INSTANCE = new LinkManager();
    private static final int MAX_LINKS = 50;
    private final Map<String, Link> allLinks = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(LinkManager.class);

    private LinkManager() {
    }

    static LinkManager getInstance() {
        return INSTANCE;
    }

    Link createLink(User from, User to, String url, String title, String favIconURL) {
        EventPlexer plexer = EventPlexer.getInstance();

        String id = Globals.generateId();

        Link link = new Link(from, to, id, url, title, favIconURL);

        synchronized (allLinks) {
            allLinks.put(link.getId(), link);
            if (allLinks.size() > MAX_LINKS) {
                plexer.queueLink(EventPlexer.Action.DELETED, allLinks.remove(0));
            }
        }

        plexer.queueLink(EventPlexer.Action.CREATED, link);

        return link;
    }

    List<Link> getLinks(User user) {
        List<Link> result = new ArrayList<>();

        synchronized (allLinks) {
            for (Link l : allLinks.values()) {

                if (l.getTo() == null) {
                    log.warn("Missing to for link {}", l.toJson());
                    continue;
                }
                if (l.getFrom() == null) {
                    log.warn("Missing from for link {}", l.toJson());
                    continue;
                }

                if (l.getTo().equals(user) || l.getFrom().equals(user)) {
                    result.add(l);
                }
            }
        }
        return result;
    }

    Link deleteLink(User user, String id) {
        synchronized (allLinks) {
            Link link = allLinks.get(id);
            if (link != null && link.getFrom().equals(user)) {
                allLinks.remove(id);
                return link;
            } else {
                return null;
            }
        }
    }

    private JsonArray getJsonLinks() {

        JsonArrayBuilder json = Json.createArrayBuilder();
        synchronized (allLinks) {
            for (Link l : allLinks.values()) {
                json.add(l.toJson());
            }
        }

        return json.build();
    }

    private void setLinks(JsonArray links) {
        List<Link> newLinks = new ArrayList<>(links.size());
        for (JsonValue raw : links) {
            JsonObject json = (JsonObject) raw;

            newLinks.add(new Link(json));
        }

        synchronized (allLinks) {
            allLinks.clear();
            for (Link l : newLinks) {
                allLinks.put(l.getId(), l);
            }
        }
    }

    void saveLinks(Properties props) throws IOException {
        JsonArray links = getJsonLinks();

        File linkFile = Globals.getFile(props, FILENAME);
        log.info("Saving {} link(s) to {}", links.size(), linkFile.getAbsolutePath());
        try (FileWriter out = new FileWriter(linkFile)) {
            out.write(links.toString());
            out.flush();
        }
    }

    void loadLinks(Properties props) throws IOException {
        File linkFile = Globals.getFile(props, FILENAME);
        try (JsonReader in = Json.createReader(new FileReader(linkFile))) {
            JsonArray links = in.readArray();
            log.info("Read {} link(s) from {}", links.size(), linkFile.getAbsolutePath());
            setLinks(links);
        }
    }


}
