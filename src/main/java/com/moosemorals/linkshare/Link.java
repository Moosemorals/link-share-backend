package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

final class Link {

    private final String link;
    private final String description;
    private final long id;
    private final long created;

    Link(String link, String description, long id) {
        this.link = link;
        this.id = id;
        this.description = description;
        this.created = System.currentTimeMillis();
    }

    Link(JsonObject json) {
        if (json.containsKey("link")) {
            this.link = json.getString("link");
        } else {
            throw new IllegalArgumentException("Missing link from json");
        }
        if (json.containsKey("id")) {
            this.id = json.getJsonNumber("id").longValue();
        } else {
            throw new IllegalArgumentException("Missing id from json");
        }
        if (json.containsKey("created")) {
            this.created = json.getJsonNumber("created").longValue();
        } else {
            throw new IllegalArgumentException("Missing created from json");
        }
        this.description = json.getString("description", null);
    }

    String getLink() {
        return link;
    }

    long getId() {
        return id;
    }

    String getDescription() {
        return description;
    }

    JsonObject toJson() {
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("link", link)
                .add("id", id)
                .add("created", created);

        if (description != null) {
            json.add("description", description);
        }

        return json.build();
    }

    public long getCreated() {
        return created;
    }
}