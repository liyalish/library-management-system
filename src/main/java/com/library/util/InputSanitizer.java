package com.library.util;

public final class InputSanitizer {
    private InputSanitizer() {
    }

    public static String clean(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[<>]", "").trim();
    }
}