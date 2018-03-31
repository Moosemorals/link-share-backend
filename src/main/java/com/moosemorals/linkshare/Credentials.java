package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;

final class Credentials {
    private final Logger log = LoggerFactory.getLogger(Credentials.class);
    private final String name;
    private final String saltAndHash;

    Credentials(JsonObject json) {
        if (!json.containsKey("name")) {
            throw new IllegalArgumentException("Missing name from JSON");
        }

        this.name = json.getString("name");

        if (!json.containsKey("password")) {
            throw new IllegalArgumentException("Missing password from JSON");
        }
        String password = json.getString("password");

        if (!json.containsKey("hashed")) {
            throw new IllegalArgumentException("Missing hashed from JSON");
        }

        boolean hashed = json.getBoolean("hashed");

        if (!hashed) {
            password = AuthManager.generateSaltAndHash(password);
        }

        this.saltAndHash = password;
    }

    String getName() {
        return name;
    }

    String getSaltAndHash() {
        return saltAndHash;
    }

    JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("hashed", true)
                .add("name", name)
                .add("password", saltAndHash)
                .add("iterations", AuthManager.DEFAULT_ITERATIONS)
                .build();
    }
}
