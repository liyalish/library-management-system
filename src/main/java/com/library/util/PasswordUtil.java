package com.library.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, storedHash);
    }
}