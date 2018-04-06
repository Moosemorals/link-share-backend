package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;

final class Credentials {
    private static final String[] REQUIRED_FIELDS = {"password", "hashed"};
    private final Logger log = LoggerFactory.getLogger(Credentials.class);
    private final String saltAndHash;

    Credentials(JsonObject json) {
        for (String field : REQUIRED_FIELDS) {
            if (!json.containsKey(field)) {
                throw new IllegalArgumentException("Missing field '" + field + "' from JSON");
            }
        }

        String password = json.getString("password");
        final boolean hashed = json.getBoolean("hashed");

        if (!hashed) {
            password = AuthManager.generateSaltAndHash(password);
        }

        this.saltAndHash = password;
    }

    String getSaltAndHash() {
        return saltAndHash;
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("hashed", true)
                .add("password", saltAndHash)
                .build();
    }
}
