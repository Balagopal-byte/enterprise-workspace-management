package com.workspace.management.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Date and time formatting utility for consistent display across the application.
 */
public final class DateTimeUtil {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    public static final DateTimeFormatter SQL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter SQL_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private DateTimeUtil() {}

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMAT) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMAT) : "";
    }

    public static String formatTimeRange(LocalTime start, LocalTime end) {
        return formatTime(start) + " - " + formatTime(end);
    }

    /**
     * Generate time options for combo boxes (every 30 minutes).
     */
    public static LocalTime[] generateTimeSlots() {
        LocalTime[] slots = new LocalTime[34]; // 7:00 AM to 11:30 PM
        LocalTime time = LocalTime.of(7, 0);
        for (int i = 0; i < slots.length; i++) {
            slots[i] = time;
            time = time.plusMinutes(30);
        }
        return slots;
    }

    /**
     * Check if a date is today or in the future.
     */
    public static boolean isTodayOrFuture(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    /**
     * Get a human-readable relative date string.
     */
    public static String getRelativeDateString(LocalDate date) {
        if (date == null) return "";
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "Today";
        if (date.equals(today.plusDays(1))) return "Tomorrow";
        if (date.equals(today.minusDays(1))) return "Yesterday";
        return formatDate(date);
    }
}
