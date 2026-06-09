package com.library.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords using the BCrypt algorithm.
 * Plain-text passwords are never stored; only their BCrypt hashes.
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // Utility class — no instances.
    }

    /**
     * Hashes a plain-text password using BCrypt with a generated salt.
     *
     * @param plainPassword the raw password entered by the user
     * @return the BCrypt hash to store in the database
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainPassword the raw password to check
     * @param storedHash    the BCrypt hash from the database
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, storedHash);
    }
}