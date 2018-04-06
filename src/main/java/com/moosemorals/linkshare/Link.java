package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

final class Link {

    private static final String[] REQUIRED_FIELDS = {"id", "url", "created", "from", "to"};

    private final String url;
    private final String id;
    private final String title;
    private final String favIconURL;
    private final long created;
    private final User from;
    private final User to;

    Link(User from, User to, String id, String link, String title, String favIconURL) {
        this.from = from;
        this.to = to;
        this.url = link;
        this.id = id;
        this.created = System.currentTimeMillis();
        this.title = title;
        this.favIconURL = favIconURL;
    }

    Link(JsonObject json) {
        for (String field : REQUIRED_FIELDS) {
            if (!json.containsKey(field)) {
                throw new IllegalArgumentException("Missing '" + field + "' from JSON");
            }
        }
        // required fields
        this.url = json.getString("url");
        this.id = json.getJsonString("id").getString();
        this.created = json.getJsonNumber("created").longValue();
        this.from = new User(json.get("from"));
        this.to = new User(json.get("to"));
        // optional fields
        this.title = json.getString("title", null);
        this.favIconURL = json.getString("favIconURL", null);
    }

    String getId() {
        return id;
    }

    JsonObject toJson() {
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("url", url)
                .add("id", id)
                .add("created", created)
                .add("from", from.toJson())
                .add("to", to.toJson());

        if (title != null) {
            json.add("title", title);
        }

        if (favIconURL != null) {
            json.add("favIconURL", favIconURL);
        }

        return json.build();
    }

    long getCreated() {
        return created;
    }

    User getFrom() {
        return from;
    }

    User getTo() {
        return to;
    }
}