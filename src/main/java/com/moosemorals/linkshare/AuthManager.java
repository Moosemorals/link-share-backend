package com.moosemorals.linkshare;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.json.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

final class AuthManager {

    static final int DEFAULT_ITERATIONS = 30 * 1000;
    private static final File AUTH_DATABASE = new File("/home/osric/tmp/auth.json");
    private static final int DEFAULT_KEYLENGTH = 256;

    private static final AuthManager INSTANCE = new AuthManager();

    private static final SecureRandom random = new SecureRandom();
    private final Map<String, Credentials> creds = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);

    private AuthManager() {
    }

    static AuthManager getInstance() {
        return INSTANCE;
    }

    static String generateSaltAndHash(String password) {

        byte[] salt = new byte[64];

        random.nextBytes(salt);

        byte[] hash = hashPassword(
                password.toCharArray(),
                salt,
                DEFAULT_ITERATIONS,
                DEFAULT_KEYLENGTH
        );

        Base64 encoder = new Base64();

        return encoder.encodeToString(salt) + ":" + encoder.encodeToString(hash);
    }

    static boolean checkPassword(String saltAndHash, String given) {
        Base64 decoder = new Base64();
        String[] parts = saltAndHash.split(":");

        byte[] salt = decoder.decode(parts[0]);

        byte[] hash = hashPassword(
                given.toCharArray(),
                salt,
                DEFAULT_ITERATIONS,
                DEFAULT_KEYLENGTH
        );

        byte[] passwordHash = decoder.decode(parts[1]);

        boolean result = true;
        for (int i = 0; i < passwordHash.length; i += 1) {
            if (hash[i] != passwordHash[i]) {
                result = false;
            }
        }
        return result;
    }

    private static byte[] hashPassword(final char[] password, final byte[] salt, final int iterations, final int keyLength) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            return key.getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    boolean checkAuth(String name, String password) {
        Credentials c;
        synchronized (creds) {
            c = creds.get(name);
        }
        if (c == null) {
            return false;
        }

        return checkPassword(c.getSaltAndHash(), password);
    }

    void saveDatabase() throws IOException {
        JsonArrayBuilder json = Json.createArrayBuilder();
        synchronized (creds) {
            for (Credentials c : creds.values()) {
                json.add(c.toJson());
            }
        }

        try (JsonWriter out = Json.createWriter(new FileWriter(AUTH_DATABASE))) {
            out.write(json.build());
        }
    }

    void loadDatabase() throws IOException {
        JsonArray credsArray;
        try (JsonReader in = Json.createReader(new FileReader(AUTH_DATABASE))) {
            credsArray = in.readArray();
        }
        synchronized (creds) {
            creds.clear();
            for (JsonValue v : credsArray) {
                Credentials c = new Credentials(v.asJsonObject());
                creds.put(c.getName(), c);
            }
        }
    }
}