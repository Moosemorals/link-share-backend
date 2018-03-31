package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

final class User {
    private final String name;

    User(String name) {
        this.name = name;
    }

    User(JsonValue json) {
        if (json.getValueType() == JsonValue.ValueType.STRING) {
            name = ((JsonString) json).getString();
        } else {
            throw new IllegalArgumentException("That really should have been a string");
        }
    }

    String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    JsonValue toJson() {
        return Json.createValue(name);
    }
}
