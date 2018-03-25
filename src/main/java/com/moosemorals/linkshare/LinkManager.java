package com.moosemorals.linkshare;

import javax.json.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

final class LinkManager {

    private static final LinkManager INSTANCE = new LinkManager();

    private static final int MAX_LINKS = 50;
    static LinkManager getInstance() {
        return INSTANCE;
    }

    private final AtomicLong linkIds = new AtomicLong(0);
    private final List<Link> allLinks = new ArrayList<>();

    private LinkManager() {

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
        for (Link l : links ) {
            json.add(l.toJson());
        }

        return json.build();
    }

    public void setLinks(JsonArray links) {
        List<Link> newLinks = new ArrayList<>(links.size());
        for (JsonValue raw : links) {
            JsonObject json = raw.asJsonObject();

            newLinks.add( new Link(json));
        }

        synchronized (allLinks) {
            allLinks.clear();
            allLinks.addAll(newLinks);
        }
    }
}
