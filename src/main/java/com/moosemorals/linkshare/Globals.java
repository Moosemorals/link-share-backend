package com.moosemorals.linkshare;

import org.apache.commons.codec.binary.Base64;

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
}
