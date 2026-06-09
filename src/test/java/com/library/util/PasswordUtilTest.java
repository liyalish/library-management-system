package com.library.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PasswordUtil}, verifying BCrypt hashing and verification.
 */
class PasswordUtilTest {

    @Test
    void hash_thenMatches_returnsTrue() {
        String hash = PasswordUtil.hash("myPassword1");
        assertTrue(PasswordUtil.matches("myPassword1", hash));
    }

    @Test
    void matches_withWrongPassword_returnsFalse() {
        String hash = PasswordUtil.hash("myPassword1");
        assertFalse(PasswordUtil.matches("wrongPassword", hash));
    }

    @Test
    void hash_sameInput_producesDifferentHashes() {
        // BCrypt uses a random salt, so two hashes of the same password differ.
        String h1 = PasswordUtil.hash("samePass");
        String h2 = PasswordUtil.hash("samePass");
        assertNotEquals(h1, h2);
    }

    @Test
    void matches_withNullHash_returnsFalse() {
        assertFalse(PasswordUtil.matches("anything", null));
    }
}