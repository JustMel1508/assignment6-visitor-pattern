package com.example.codeinspection.model;

import java.util.Objects;

final class Validation {
    private Validation() { }

    static String name(String value, String label) {
        Objects.requireNonNull(value, label);
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value.strip();
    }

    static void nonnegative(int value, String label) {
        if (value < 0) {
            throw new IllegalArgumentException(label + " must be nonnegative");
        }
    }
}
