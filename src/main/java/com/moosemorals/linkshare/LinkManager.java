package com.moosemorals.linkshare;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

final class LinkManager {

    private static final LinkManager INSTANCE = new LinkManager();

    static LinkManager getInstance() {
        return INSTANCE;
    }

    private final AtomicLong linkIds = new AtomicLong(0);
    private final List<Link> allLinks = new ArrayList<>();

    private LinkManager() {

    }

    Link createLink(String linkDestination) {
        long id = linkIds.incrementAndGet();

        Link link = new Link(linkDestination, id);

        allLinks.add(link);

        EventPlexer.getInstance().queueLink(link);

        return link;
    }

    List<Link> getLinks(long from) {
        List<Link> result = new ArrayList<>();

        for (Link l : allLinks) {
            if (l.getId() >= from) {
                result.add(l);
            }
        }

        return result;
    }
}
