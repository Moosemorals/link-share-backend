package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonObject;

final class Token {
    private static final String[] REQUIRED_FIELDS = {"id", "device", "hashed"};

    private final String id;
    private final String device;

    Token(String id, String device) {
        this.id = AuthManager.generateSaltAndHash(id);
        this.device = device;
    }

    Token(JsonObject json) {
        for (String field : REQUIRED_FIELDS) {
            if (!json.containsKey(field)) {
                throw new IllegalArgumentException("Missing field '" + field +"' from JSON");
            }
        }

        boolean hashed = json.getBoolean("hashed");

        if (!hashed) {
            String raw = json.getString("id");
            this.id = AuthManager.generateSaltAndHash(raw);
        } else {
            this.id = json.getString("id");
        }
        this.device = json.getString("device");
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("device", device)
                .add("hashed", true)
                .build();
    }

    boolean check(String token) {
        return AuthManager.checkPassword(id, token);
    }

    String getId() {
        return id;
    }

    String getDevice() {
        return device;
    }
}
