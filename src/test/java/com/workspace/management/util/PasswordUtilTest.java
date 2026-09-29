package com.workspace.management.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PasswordUtil - BCrypt hashing and verification.
 */
class PasswordUtilTest {

    @Test
    @DisplayName("Password should be hashed and verified correctly")
    void testHashAndVerify() {
        String password = "Admin@123";
        String hash = PasswordUtil.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash); // Hash should differ from plain text
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    @DisplayName("Wrong password should not verify")
    void testWrongPassword() {
        String hash = PasswordUtil.hashPassword("CorrectPassword");
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", hash));
    }

    @Test
    @DisplayName("Null inputs should return false")
    void testNullInputs() {
        assertFalse(PasswordUtil.verifyPassword(null, "somehash"));
        assertFalse(PasswordUtil.verifyPassword("password", null));
        assertFalse(PasswordUtil.verifyPassword(null, null));
    }

    @Test
    @DisplayName("Same password produces different hashes (salt)")
    void testSaltUniqueness() {
        String password = "TestPassword123";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);
        // BCrypt generates unique salts, so hashes should differ
        assertNotEquals(hash1, hash2);
        // But both should verify
        assertTrue(PasswordUtil.verifyPassword(password, hash1));
        assertTrue(PasswordUtil.verifyPassword(password, hash2));
    }
}
