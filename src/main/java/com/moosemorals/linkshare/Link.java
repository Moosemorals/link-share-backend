package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

final class Link {

    private final String link;
    private final String description;
    private final String id;
    private final String title;
    private final String favIconURL;
    private final long created;
    private final User owner;

    Link(User owner, String id, String link, String title, String favIconURL, String description) {
        this.owner = owner;
        this.link = link;
        this.id = id;
        this.description = description;
        this.created = System.currentTimeMillis();
        this.title = title;
        this.favIconURL = favIconURL;
    }

    Link(JsonObject json) {
        if (json.containsKey("link")) {
            this.link = json.getString("link");
        } else {
            throw new IllegalArgumentException("Missing link from json");
        }
        if (json.containsKey("id")) {
            this.id = json.getJsonString("id").getString();
        } else {
            throw new IllegalArgumentException("Missing id from json");
        }
        if (json.containsKey("created")) {
            this.created = json.getJsonNumber("created").longValue();
        } else {
            throw new IllegalArgumentException("Missing created from json");
        }
        if (json.containsKey("owner")) {
            this.owner = new User(json.get("owner"));
        } else {
            throw new IllegalArgumentException("Missing owner from json");
        }
        this.description = json.getString("description", null);
        this.title = json.getString("title", null);
        this.favIconURL = json.getString("favIconUrl", null);
    }

    String getLink() {
        return link;
    }

    String getId() { return id; }

    JsonObject toJson() {
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("link", link)
                .add("id", id)
                .add("created", created)
                .add("owner", owner.toJson());

        if (description != null) {
            json.add("description", description);
        }

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
}