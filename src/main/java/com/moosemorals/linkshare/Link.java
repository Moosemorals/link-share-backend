package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        AuthManager authManager = AuthManager.getInstance();

        // required fields
        this.url = json.getString("url");
        this.id = json.getJsonString("id").getString();
        this.created = json.getJsonNumber("created").longValue();
        this.from = authManager.getUserByName(json.getString("from"));
        this.to = authManager.getUserByName(json.getString("to"));
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
                .add("created", created);

        if (to != null) {
            json.add("to", to.getName());
        } else {
            json.addNull("to");
        }

        if (from != null) {
            json.add("from", from.getName());
        } else {
            json.addNull("from");
        }

        if (title != null) {
            json.add("title", title);
        }

        if (favIconURL != null) {
            json.add("favIconURL", favIconURL);
        }

        return json.build();
    }

    boolean isRelated(User u) {
        return from.equals(u) || to.equals(u);
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