package com.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesLoader {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = PropertiesLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties not found on classpath");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application.properties", e);
        }
    }

    private PropertiesLoader() {
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static int getInt(String key, int defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}