package com.workspace.management.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 * Ensures passwords are never stored in plain text.
 */
public final class PasswordUtil {
    private static final int BCRYPT_COST = 12;

    private PasswordUtil() {} // Prevent instantiation

    /**
     * Hash a plain text password using BCrypt.
     * @param plainPassword the password to hash
     * @return BCrypt hash string
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    /**
     * Verify a plain password against a BCrypt hash.
     * @param plainPassword the password to check
     * @param hashedPassword the stored BCrypt hash
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
