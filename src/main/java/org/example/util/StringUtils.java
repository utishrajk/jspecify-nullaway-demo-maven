package org.example.util;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

@NullMarked
public class StringUtils {

    public static String combineNames(@Nullable String firstName, @Nullable String lastName) {
        if (firstName == null && lastName == null) {
            return "Unknown";
        }
        if (firstName == null) {
            return lastName != null ? lastName : "Unknown";
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    public static String getAgeGroup(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (age < 18) {
            return "Minor";
        } else if (age < 65) {
            return "Adult";
        } else {
            return "Senior";
        }
    }

    public static @Nullable String formatEmail(@Nullable String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.toLowerCase(Locale.ROOT).trim();
    }

    public static String safeToString(@Nullable Object obj) {
        return obj != null ? obj.toString() : "N/A";
    }

    public static String safeUpperCase(@Nullable String input) {
        if (input == null) {
            return "NULL_INPUT";
        }
        return input.toUpperCase(Locale.ROOT);
    }
}