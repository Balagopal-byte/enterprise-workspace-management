package com.workspace.management.exception;

/**
 * Custom exception for database operation errors.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) { super(message); }
    public DatabaseException(String message, Throwable cause) { super(message, cause); }
}
