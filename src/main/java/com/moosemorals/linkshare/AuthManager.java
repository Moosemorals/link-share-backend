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
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

final class AuthManager {

    static final int DEFAULT_ITERATIONS = 30 * 1000;
    private static final String FILENAME = "auth.json";
    private static final String PASSWORD_HASH = "PBKDF2WithHmacSHA1";
    private static final int DEFAULT_KEY_LENGTH = 256;
    private static final AuthManager INSTANCE = new AuthManager();
    private static final SecureRandom random = new SecureRandom();
    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);
    private final Map<String, Credentials> creds = new HashMap<>();
    private final Map<User, Set<String>> tokens = new HashMap<>();

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
                DEFAULT_KEY_LENGTH
        );

        Base64 encoder = new Base64();

        return encoder.encodeToString(salt) + ":" + encoder.encodeToString(hash);
    }

    private static boolean checkPassword(String saltAndHash, String given) {
        Base64 decoder = new Base64();
        String[] parts = saltAndHash.split(":");

        byte[] salt = decoder.decode(parts[0]);

        byte[] hash = hashPassword(
                given.toCharArray(),
                salt,
                DEFAULT_ITERATIONS,
                DEFAULT_KEY_LENGTH
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
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PASSWORD_HASH);
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            return key.getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    User checkAuth(String name, String password) {
        Credentials c;
        synchronized (creds) {
            c = creds.get(name);
        }
        if (c == null) {
            return null;
        }

        if (checkPassword(c.getSaltAndHash(), password)) {
            return new User(name);
        } else {
            return null;
        }
    }

    void saveDatabase(Properties props) throws IOException {

        JsonArrayBuilder json = Json.createArrayBuilder();
        synchronized (creds) {
            for (Credentials c : creds.values()) {
                json.add(c.toJson());
            }
        }
        JsonArray jsonArray = json.build();

        File authDatabase = Globals.getFile(props, FILENAME);
        log.info("Writing {} entries to {}", jsonArray.size(), authDatabase.getAbsolutePath());
        try (JsonWriter out = Json.createWriter(new FileWriter(authDatabase))) {
            out.write(jsonArray);
        }
    }

    void loadDatabase(Properties props) throws IOException {
        File authDatabase = Globals.getFile(props, FILENAME);
        JsonArray credsArray;
        try (JsonReader in = Json.createReader(new FileReader(authDatabase))) {
            credsArray = in.readArray();
        }
        log.info("Read {} entries from ", credsArray.size(), authDatabase);
        synchronized (creds) {
            creds.clear();
            for (JsonValue v : credsArray) {
                Credentials c = new Credentials((JsonObject)v);
                creds.put(c.getName(), c);
            }
        }
    }

    User checkToken(String token) {
        synchronized (tokens) {
            for (Map.Entry<User, Set<String>> entry : tokens.entrySet()) {
                for (String t : entry.getValue()) {
                    if (t.equals(token)) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    String createToken(User user) {
        String token = Globals.generateId();
        synchronized (tokens) {
            if (!tokens.containsKey(user)) {
                tokens.put(user, new HashSet<String>());
            }

            tokens.get(user).add(token);
        }
        return token;
    }

    void logout(User user, String token) {
        synchronized (tokens) {
            if (tokens.containsKey(user)) {
                if (token != null) {
                    Set<String> userTokens = tokens.get(user);
                    userTokens.remove(token);
                    if (userTokens.isEmpty()) {
                        tokens.remove(user);
                    }
                } else {
                    tokens.remove(user);
                }
            }
        }
    }

    User lookupUser(String to) {
        String userName = to.toLowerCase();
        synchronized (creds) {
            if (creds.containsKey(userName)) {
                return new User(userName);
            }
        }
        return null;
    }
}
