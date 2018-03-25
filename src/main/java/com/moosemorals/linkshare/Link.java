package com.moosemorals.linkshare;

final class Link {

    private final String link;
    private final long id;

    Link(String link, long id) {
        this.link = link;
        this.id = id;
    }

    String getLink() {
        return link;
    }

    long getId() {
        return id;
    }

}