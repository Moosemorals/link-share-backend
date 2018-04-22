package com.moosemorals.linkshare;

import javax.json.*;
import java.util.*;

final class User {
    private static final String[] REQUIRED_FIELDS = {"name","creds","tokens"};

    private final String name;
    private final Credentials creds;
    private final Set<Token> tokens;
    private final Set<String> phones;

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

        phones = new HashSet<>();
        if (json.containsKey("phones")) {
            for (JsonValue j : json.getJsonArray("phones")) {
                JsonString t = (JsonString)j;
                phones.add(t.getString());
            }
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
           if (t.check(token)) {
               return t;
           }
       }
       return null;
    }

    void invalidateToken(String token) {
        for (Iterator<Token> iterator = tokens.iterator(); iterator.hasNext(); ) {
            Token t = iterator.next();
            if (t.check(token)) {
                iterator.remove();
            }
        }
    }

    Token addToken(String device, String id) {
       for (Token t : tokens) {
           if (t.getDevice().equals(device)) {
               return t;
           }
       }
       Token t = new Token(id, device);
       tokens.add(t);
       return t;
    }

    void addPhone(String phone) {
       phones.add(phone);
    }

    Collection<String> getPhones() {
       return Collections.unmodifiableSet(phones);
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

        JsonArrayBuilder jsonPhones = Json.createArrayBuilder();
        for (String t : phones) {
            jsonPhones.add(t);
        }

        return Json.createObjectBuilder()
                .add("name", name)
                .add("creds", creds.toJson())
                .add("tokens", jsonTokens.build())
                .add("phones", jsonPhones.build())
                .build();
    }
}
