package com.moosemorals.linkshare;

import javax.json.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

final class User {
    private static final String[] REQUIRED_FIELDS = {"name","creds","tokens"};

    private final String name;
    private final Credentials creds;
    private final Set<Token> tokens;

   User(JsonObject json) {
        for (String field : REQUIRED_FIELDS) {
            if (!json.containsKey(field)) {
                throw new IllegalArgumentException("Missing field '" + field + "' from JSON");
            }
        }

        name = json.getString("name");
        creds = new Credentials(json.getJsonObject("creds"));
        tokens = new HashSet<>();

        for (JsonValue j : json.getJsonArray("tokens")) {
            JsonObject jsonToken = (JsonObject)j;
            tokens.add(new Token(jsonToken));
        }
    }

    String getSaltAndHash() {
       return creds.getSaltAndHash();
    }

    String getName() {
        return name;
    }

    boolean matchesName(String name) {
       if (name == null || name.isEmpty()) {
           return false;
       }
       return this.name.toLowerCase().equals(name.toLowerCase());
    }

    Token getToken(String token) {
       for (Token t : tokens) {
           if (t.getId().equals(token)) {
               return t;
           }
       }
       return null;
    }

    void invalidateToken(String token) {
        for (Iterator<Token> iterator = tokens.iterator(); iterator.hasNext(); ) {
            Token t = iterator.next();
            if (t.getId().equals(token)) {
                iterator.remove();
            }
        }
    }

    Token addToken(String device) {
       for (Token t : tokens) {
           if (t.getDevice().equals(device)) {
               return t;
           }
       }
       Token t = new Token(Globals.generateId(), device);
       tokens.add(t);
       return t;
    }

    JsonArray getDeviceJson() {
       JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
       for (Token t : tokens) {
           jsonArrayBuilder.add(t.getDevice());
       }
       return jsonArrayBuilder.build();
    }

    JsonObject toJson() {
        JsonArrayBuilder jsonTokens = Json.createArrayBuilder();
        for (Token t : tokens) {
            jsonTokens.add(t.toJson());
        }

        return Json.createObjectBuilder()
                .add("name", name)
                .add("creds", creds.toJson())
                .add("tokens", jsonTokens.build())
                .build();
    }
}
