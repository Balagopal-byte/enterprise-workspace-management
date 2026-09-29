package com.workspace.management.exception;

/**
 * Exception for authentication failures (invalid credentials, disabled accounts, etc.)
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) { super(message); }
    public AuthenticationException(String message, Throwable cause) { super(message, cause); }
}
