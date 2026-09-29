package com.workspace.management.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Booking model - especially overlap detection logic.
 */
class BookingTest {

    @Test
    @DisplayName("Overlapping intervals should be detected")
    void testOverlappingIntervals() {
        Booking booking = new Booking(1, 1, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(14, 0));

        // Overlapping cases
        assertTrue(booking.overlaps(LocalTime.of(9, 0), LocalTime.of(11, 0)));   // Starts before, ends during
        assertTrue(booking.overlaps(LocalTime.of(12, 0), LocalTime.of(16, 0)));   // Starts during, ends after
        assertTrue(booking.overlaps(LocalTime.of(11, 0), LocalTime.of(13, 0)));   // Entirely within
        assertTrue(booking.overlaps(LocalTime.of(9, 0), LocalTime.of(16, 0)));    // Entirely encompasses
        assertTrue(booking.overlaps(LocalTime.of(10, 0), LocalTime.of(14, 0)));   // Exact match
    }

    @Test
    @DisplayName("Non-overlapping intervals should not be detected")
    void testNonOverlappingIntervals() {
        Booking booking = new Booking(1, 1, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(14, 0));

        // Non-overlapping cases
        assertFalse(booking.overlaps(LocalTime.of(7, 0), LocalTime.of(10, 0)));   // Ends exactly when booking starts
        assertFalse(booking.overlaps(LocalTime.of(14, 0), LocalTime.of(18, 0)));  // Starts exactly when booking ends
        assertFalse(booking.overlaps(LocalTime.of(6, 0), LocalTime.of(9, 0)));    // Entirely before
        assertFalse(booking.overlaps(LocalTime.of(15, 0), LocalTime.of(18, 0)));  // Entirely after
    }

    @Test
    @DisplayName("Booking status transitions work correctly")
    void testBookingStatus() {
        Booking booking = new Booking();

        booking.setStatus("CONFIRMED");
        assertTrue(booking.canCheckIn());
        assertTrue(booking.canCancel());
        assertTrue(booking.isActive());

        booking.setStatus("CHECKED_IN");
        assertFalse(booking.canCheckIn());
        assertFalse(booking.canCancel());
        assertTrue(booking.isActive());

        booking.setStatus("CANCELLED");
        assertFalse(booking.canCheckIn());
        assertFalse(booking.canCancel());
        assertFalse(booking.isActive());

        booking.setStatus("AUTO_RELEASED");
        assertFalse(booking.isActive());
    }

    @Test
    @DisplayName("Role-based permissions work correctly")
    void testRolePermissions() {
        User admin = new User();
        admin.setRoleId(1); // SYSTEM_ADMIN

        User manager = new User();
        manager.setRoleId(2); // FACILITY_MANAGER

        User employee = new User();
        employee.setRoleId(3); // EMPLOYEE

        assertTrue(admin.isAdmin());
        assertTrue(admin.hasRole(Role.SYSTEM_ADMIN));
        assertTrue(admin.hasRole(Role.FACILITY_MANAGER));
        assertTrue(admin.hasRole(Role.EMPLOYEE));

        assertFalse(manager.isAdmin());
        assertTrue(manager.isManager());
        assertTrue(manager.hasRole(Role.FACILITY_MANAGER));
        assertTrue(manager.hasRole(Role.EMPLOYEE));
        assertFalse(manager.hasRole(Role.SYSTEM_ADMIN));

        assertFalse(employee.isAdmin());
        assertFalse(employee.isManager());
        assertTrue(employee.isEmployee());
        assertTrue(employee.hasRole(Role.EMPLOYEE));
        assertFalse(employee.hasRole(Role.FACILITY_MANAGER));
    }
}
