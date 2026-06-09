package com.library.util;

/**
 * Small helper for sanitizing free-text user input before it is stored or processed,
 * as an extra defense-in-depth measure against script injection (XSS). Note that the
 * main XSS protection is Thymeleaf's automatic HTML escaping on output (th:text), and
 * SQL injection is prevented by PreparedStatements in the DAO layer; this utility adds
 * an additional input-side safeguard.
 */
public final class InputSanitizer {

    private InputSanitizer() {
        // Utility class — no instances.
    }

    /**
     * Strips angle brackets and trims input to neutralize attempts to inject HTML or
     * script tags through form fields.
     *
     * @param input the raw user input (may be null)
     * @return a sanitized string, or null if the input was null
     */
    public static String clean(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[<>]", "").trim();
    }
}