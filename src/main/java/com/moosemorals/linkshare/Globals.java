package com.moosemorals.linkshare;

import org.apache.commons.codec.binary.Base64;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Properties;

final class Globals {
    final static String BASE_PATH_KEY = "basePath";

    private static final int ID_BYTES = 30; // Should be a multiple of 4 and 3
    private static final SecureRandom random = new SecureRandom();

    static File getFile(Properties props, String name) throws IOException {
        String basePath = props.getProperty(Globals.BASE_PATH_KEY);
        if (basePath == null) {
            throw new IOException("Can't get base path from properties");
        }
        return new File(basePath, name);
    }

    static String generateId() {
        byte[] bytes = new byte[ID_BYTES];
        random.nextBytes(bytes);
        return Base64.encodeBase64URLSafeString(bytes);
    }

    static boolean validateId(String id) {
        if (id == null || id.length() != 40) {
            return false;
        }

        return Base64.isBase64(id);
    }

    static void sendError(HttpServletResponse resp, String message) throws IOException {
        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    static void sendError(HttpServletResponse resp, int status,  String message) throws IOException {
        setupResponse(resp);
        resp.setStatus(status);
        try (JsonWriter jOut = Json.createWriter(resp.getWriter())) {
            jOut.write(Json.createObjectBuilder()
                    .add("error", message)
                    .build());
        }
    }

    static void sendSuccess(HttpServletResponse resp, JsonValue success) throws IOException {
        setupResponse(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        try (JsonWriter jOut = Json.createWriter(resp.getWriter())) {
            jOut.write(Json.createObjectBuilder()
                    .add("success", success)
                    .build());
        }
    }

    static void sendSuccess(HttpServletResponse resp, String... message) throws IOException {
        if (message.length == 1) {
            sendSuccess(resp, Json.createValue(message[0]));
        } else if (message.length %2 == 0) {
            JsonObjectBuilder json = Json.createObjectBuilder();
            for (int i = 0; i < message.length; i += 2) {
                json.add(message[i], message[i+1]);
            }
            sendSuccess(resp, json.build());
        }
    }

    private static void setupResponse(HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");
    }
}
