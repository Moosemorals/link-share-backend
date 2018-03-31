package com.moosemorals.linkshare;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

final class Globals {
    public final static String BASE_PATH_KEY = "basePath";

     static File getFile(Properties props, String name) throws IOException {
        String basePath = props.getProperty(Globals.BASE_PATH_KEY);
        if (basePath == null) {
            throw new IOException("Can't get base path from properties");
        }
        return new File(basePath, name);
    }
}
