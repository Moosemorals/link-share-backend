package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

final class LinkManager {

    private static final String FILENAME = "links.json";
    private static final LinkManager INSTANCE = new LinkManager();
    private static final int MAX_LINKS = 50;
    private final AtomicLong linkIds = new AtomicLong(0);
    private final List<Link> allLinks = new ArrayList<>();
    private final Logger log = LoggerFactory.getLogger(LinkManager.class);
    private LinkManager() {

    }

    static LinkManager getInstance() {
        return INSTANCE;
    }

    Link createLink(String linkDestination, String description) {
        long id = linkIds.incrementAndGet();

        Link link = new Link(linkDestination, description, id);

        synchronized (allLinks) {
            allLinks.add(link);
            if (allLinks.size() > MAX_LINKS) {
                allLinks.remove(0);
            }
        }

        EventPlexer.getInstance().queueLink(link);

        return link;
    }

    List<Link> getLinks(long from) {
        List<Link> result = new ArrayList<>();

        synchronized (allLinks) {
            for (Link l : allLinks) {
                if (l.getCreated() >= from) {
                    result.add(l);
                }
            }
        }

        return result;
    }

    JsonArray getJsonLinks(long from) {
        List<Link> links = getLinks(from);

        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Link l : links) {
            json.add(l.toJson());
        }

        return json.build();
    }

    void setLinks(JsonArray links) {
        List<Link> newLinks = new ArrayList<>(links.size());
        for (JsonValue raw : links) {
            JsonObject json = raw.asJsonObject();

            newLinks.add(new Link(json));
        }

        synchronized (allLinks) {
            allLinks.clear();
            allLinks.addAll(newLinks);
        }
    }

    void saveLinks(Properties props) throws IOException {
        JsonArray links = getJsonLinks(0);

        log.debug("Saving {} link(s)", links.size());
        File linkFile = Globals.getFile(props, FILENAME);
        try (FileWriter out = new FileWriter(linkFile)) {
            out.write(links.toString());
            out.flush();
        }
    }

    void loadLinks(Properties props) throws IOException {
        File linkFile = Globals.getFile(props, FILENAME);
        try (JsonReader in = Json.createReader(new FileReader(linkFile))) {
            JsonArray links = in.readArray();
            setLinks(links);
        }
    }
}
