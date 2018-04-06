package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonObject;

final class Token {
    private static final String[] REQUIRED_FIELDS = {"id", "device"};

    private final String id;
    private final String device;

    Token(String id, String device) {
        this.id = id;
        this.device = device;
    }

    Token(JsonObject jsonToken) {
        for (String field : REQUIRED_FIELDS) {
            if (!jsonToken.containsKey(field)) {
                throw new IllegalArgumentException("Missing field '" + field +"' from JSON");
            }
        }
        this.id = jsonToken.getString("id");
        this.device = jsonToken.getString("device");
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("device", device)
                .build();
    }

    String getId() {
        return id;
    }

    String getDevice() {
        return device;
    }
}
