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
import java.util.*;

final class AuthManager {

    private static final int DEFAULT_ITERATIONS = 30 * 1000;
    private static final String FILENAME = "auth.json";
    private static final String PASSWORD_HASH = "PBKDF2WithHmacSHA1";
    private static final int DEFAULT_KEY_LENGTH = 256;
    private static final AuthManager INSTANCE = new AuthManager();
    private static final SecureRandom random = new SecureRandom();
    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);
    private static final List<User> users = new ArrayList<>();

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
       synchronized (users) {
           for (User u : users) {
               if (u.matchesName(name)) {
                   if (checkPassword(u.getSaltAndHash(), password)) {
                       return u;
                   } else {
                       return null;
                   }
               }
           }
       }
       return null;
    }

    void saveDatabase(Properties props) throws IOException {

        JsonArrayBuilder jsonUsersBuilders = Json.createArrayBuilder();
        synchronized (users) {
            for (User user : users) {
                jsonUsersBuilders.add(user.toJson());
            }
        }

        JsonArray jsonUsers = jsonUsersBuilders.build();

        File authDatabase = Globals.getFile(props, FILENAME);
        log.info("Writing {} entries to {}", jsonUsers.size(), authDatabase.getAbsolutePath());
        try (JsonWriter out = Json.createWriter(new FileWriter(authDatabase))) {
            out.write(jsonUsers);
        }
    }

    void loadDatabase(Properties props) throws IOException {
        File authDatabase = Globals.getFile(props, FILENAME);
        JsonArray jsonUsers;
        try (JsonReader in = Json.createReader(new FileReader(authDatabase))) {
            jsonUsers = in.readArray();
        }
        log.info("Read {} entries from ", jsonUsers.size(), authDatabase);
        synchronized (users) {
            users.clear();
            for (JsonValue v : jsonUsers) {
                users.add(new User((JsonObject) v));
            }
        }
    }

    User checkToken(String token) {
        synchronized (users) {
            for (User u : users) {
                Token t = u.getToken(token);
                if (t != null) {
                    return u;
                }
            }
        }
        return null;
    }

    Token createToken(User user, String device) {
        return user.addToken(device);
    }

    void logout(User user, String tokenId) {
        user.invalidateToken(tokenId);
    }

    User getUserByName(String name) {
        synchronized (users) {
            for (User u : users) {
                if (u.matchesName(name)) {
                    return u;
                }
            }
        }
        return null;
    }

    JsonArray getUsers() {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        synchronized (users) {
            for (User u : users) {
                jsonArrayBuilder.add(
                        Json.createObjectBuilder()
                                .add("name", u.getName())
                                .add("devices", u.getDeviceJson())
                                .build()
                );
            }
        }
        return jsonArrayBuilder.build();
    }
}
