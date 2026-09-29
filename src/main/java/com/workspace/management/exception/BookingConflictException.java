package com.workspace.management.exception;

/**
 * Exception thrown when a booking conflict is detected (overlapping reservations).
 */
public class BookingConflictException extends Exception {
    public BookingConflictException(String message) { super(message); }
    public BookingConflictException(String message, Throwable cause) { super(message, cause); }
}
