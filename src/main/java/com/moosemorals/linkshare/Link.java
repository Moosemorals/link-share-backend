package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

final class Link {

    private final String url;
    private final String id;
    private final String title;
    private final String favIconURL;
    private final long created;
    private final User owner;

    Link(User owner, String id, String link, String title, String favIconURL) {
        this.owner = owner;
        this.url = link;
        this.id = id;
        this.created = System.currentTimeMillis();
        this.title = title;
        this.favIconURL = favIconURL;
    }

    Link(JsonObject json) {
        if (json.containsKey("url")) {
            this.url = json.getString("url");
        } else if (json.containsKey("link")) {
            this.url = json.getString("link");
        } else {
            throw new IllegalArgumentException("Missing url from json");
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
        this.title = json.getString("title", null);
        this.favIconURL = json.getString("favIconUrl", null);
    }

    String getId() { return id; }

    JsonObject toJson() {
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("url", url)
                .add("id", id)
                .add("created", created)
                .add("owner", owner.toJson());

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

    User getOwner() {
        return owner;
    }
}