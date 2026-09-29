package com.workspace.management.util;

import java.util.regex.Pattern;

/**
 * Reusable input validation utility.
 * Prevents invalid data from reaching the database layer.
 */
public final class ValidationUtil {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z\\s'-]{2,100}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9\\s()-]{7,20}$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private ValidationUtil() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidPhone(String phone) {
        return phone == null || phone.trim().isEmpty() || PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isPositiveInt(int value) {
        return value > 0;
    }

    public static boolean isValidCapacity(int capacity) {
        return capacity >= 1 && capacity <= 500;
    }

    /**
     * Sanitize input to prevent XSS (basic HTML entity encoding).
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }

    /**
     * Validate booking: end must be after start, duration within limits.
     */
    public static String validateBookingTimes(java.time.LocalTime start, java.time.LocalTime end,
                                               int minMinutes, int maxHours) {
        if (start == null || end == null) return "Start and end times are required.";
        if (!end.isAfter(start)) return "End time must be after start time.";
        long minutes = java.time.Duration.between(start, end).toMinutes();
        if (minutes < minMinutes) return "Booking must be at least " + minMinutes + " minutes.";
        if (minutes > maxHours * 60L) return "Booking cannot exceed " + maxHours + " hours.";
        return null; // valid
    }
}
