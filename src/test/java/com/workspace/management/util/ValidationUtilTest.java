package com.workspace.management.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ValidationUtil - input validation.
 */
class ValidationUtilTest {

    @Test
    @DisplayName("Valid emails should pass validation")
    void testValidEmails() {
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("admin@company.org"));
        assertTrue(ValidationUtil.isValidEmail("test.user@domain.co.uk"));
    }

    @Test
    @DisplayName("Invalid emails should fail validation")
    void testInvalidEmails() {
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("notanemail"));
        assertFalse(ValidationUtil.isValidEmail("@example.com"));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    @DisplayName("Valid names should pass validation")
    void testValidNames() {
        assertTrue(ValidationUtil.isValidName("John"));
        assertTrue(ValidationUtil.isValidName("Jane Doe"));
        assertTrue(ValidationUtil.isValidName("O'Brien"));
    }

    @Test
    @DisplayName("Invalid names should fail validation")
    void testInvalidNames() {
        assertFalse(ValidationUtil.isValidName(""));
        assertFalse(ValidationUtil.isValidName("A"));
        assertFalse(ValidationUtil.isValidName(null));
    }

    @Test
    @DisplayName("Password validation works correctly")
    void testPasswordValidation() {
        assertTrue(ValidationUtil.isValidPassword("password123"));
        assertTrue(ValidationUtil.isValidPassword("Admin@123"));
        assertFalse(ValidationUtil.isValidPassword("short"));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    @Test
    @DisplayName("Booking time validation detects invalid intervals")
    void testBookingTimeValidation() {
        // Valid booking
        assertNull(ValidationUtil.validateBookingTimes(
                LocalTime.of(9, 0), LocalTime.of(17, 0), 30, 10));

        // End before start
        assertNotNull(ValidationUtil.validateBookingTimes(
                LocalTime.of(17, 0), LocalTime.of(9, 0), 30, 10));

        // Too short
        assertNotNull(ValidationUtil.validateBookingTimes(
                LocalTime.of(9, 0), LocalTime.of(9, 15), 30, 10));

        // Null inputs
        assertNotNull(ValidationUtil.validateBookingTimes(null, LocalTime.of(17, 0), 30, 10));
    }
}
