package com.fundfy.shared.config;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = ConfigLoader.class.getClassLoader()
                 .getResourceAsStream("config/config.properties")) {
            if (in == null) throw new RuntimeException("missing config properties!");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        String val = props.getProperty(key);
        if (val == null) throw new IllegalArgumentException("Missing config: " + key);
        return val;
    }
}
