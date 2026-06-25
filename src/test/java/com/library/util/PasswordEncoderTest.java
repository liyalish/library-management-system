package com.library.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void encodeThenMatchesReturnsTrue() {
        String hash = encoder.encode("myPassword1");

        assertTrue(encoder.matches("myPassword1", hash));
    }

    @Test
    void matchesWithWrongPasswordReturnsFalse() {
        String hash = encoder.encode("myPassword1");

        assertFalse(encoder.matches("wrongPassword", hash));
    }

    @Test
    void encodeSameInputProducesDifferentHashes() {
        String firstHash = encoder.encode("samePass");
        String secondHash = encoder.encode("samePass");

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void matchesWithInvalidHashReturnsFalse() {
        assertFalse(encoder.matches("anything", "not-a-valid-bcrypt-hash"));
    }
}
