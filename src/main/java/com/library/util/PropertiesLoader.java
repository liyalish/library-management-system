package com.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration values from {@code application.properties} on the classpath.
 * Provides simple typed access to database and pool settings.
 */
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
        // Utility class — no instances.
    }

    /**
     * Returns the property value for the given key.
     *
     * @param key the property key
     * @return the property value, or {@code null} if not present
     */
    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    /**
     * Returns the property value parsed as an integer.
     *
     * @param key          the property key
     * @param defaultValue value to use if the key is missing or unparseable
     * @return the parsed integer value
     */
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