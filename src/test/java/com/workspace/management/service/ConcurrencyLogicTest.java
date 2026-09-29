package com.workspace.management.service;

import com.workspace.management.model.Booking;
import com.workspace.management.model.MeetingRoomBooking;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for concurrency and overlap detection logic.
 * These are unit tests that verify the overlap detection algorithm.
 * The database-level concurrency test is in ConcurrencyTest.
 */
class ConcurrencyLogicTest {

    /**
     * Interval overlap formula:
     * Two intervals [s1, e1) and [s2, e2) overlap when:
     *   s1 < e2 AND e1 > s2
     */
    @Test
    @DisplayName("Verify overlap detection formula: existing.start < requested.end AND existing.end > requested.start")
    void testOverlapFormula() {
        // Simulate: existing booking 10:00 - 14:00
        LocalTime existingStart = LocalTime.of(10, 0);
        LocalTime existingEnd = LocalTime.of(14, 0);

        // Test case 1: Requested 09:00 - 11:00 → OVERLAP
        assertTrue(overlapCheck(existingStart, existingEnd, LocalTime.of(9, 0), LocalTime.of(11, 0)));

        // Test case 2: Requested 13:00 - 15:00 → OVERLAP
        assertTrue(overlapCheck(existingStart, existingEnd, LocalTime.of(13, 0), LocalTime.of(15, 0)));

        // Test case 3: Requested 11:00 - 13:00 → OVERLAP (inside)
        assertTrue(overlapCheck(existingStart, existingEnd, LocalTime.of(11, 0), LocalTime.of(13, 0)));

        // Test case 4: Requested 08:00 - 10:00 → NO OVERLAP (adjacent)
        assertFalse(overlapCheck(existingStart, existingEnd, LocalTime.of(8, 0), LocalTime.of(10, 0)));

        // Test case 5: Requested 14:00 - 16:00 → NO OVERLAP (adjacent)
        assertFalse(overlapCheck(existingStart, existingEnd, LocalTime.of(14, 0), LocalTime.of(16, 0)));

        // Test case 6: Requested 06:00 - 08:00 → NO OVERLAP (before)
        assertFalse(overlapCheck(existingStart, existingEnd, LocalTime.of(6, 0), LocalTime.of(8, 0)));

        // Test case 7: Requested 15:00 - 18:00 → NO OVERLAP (after)
        assertFalse(overlapCheck(existingStart, existingEnd, LocalTime.of(15, 0), LocalTime.of(18, 0)));

        // Test case 8: Requested 10:00 - 14:00 → OVERLAP (exact match)
        assertTrue(overlapCheck(existingStart, existingEnd, LocalTime.of(10, 0), LocalTime.of(14, 0)));

        // Test case 9: Requested 08:00 - 16:00 → OVERLAP (encompasses)
        assertTrue(overlapCheck(existingStart, existingEnd, LocalTime.of(8, 0), LocalTime.of(16, 0)));
    }

    @Test
    @DisplayName("Booking model overlap method should work correctly")
    void testBookingOverlapMethod() {
        Booking booking = new Booking();
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(14, 0));

        assertTrue(booking.overlaps(LocalTime.of(9, 0), LocalTime.of(11, 0)));
        assertFalse(booking.overlaps(LocalTime.of(14, 0), LocalTime.of(16, 0)));
    }

    @Test
    @DisplayName("MeetingRoomBooking overlap method should work correctly")
    void testMeetingRoomBookingOverlap() {
        MeetingRoomBooking booking = new MeetingRoomBooking();
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(12, 0));

        assertTrue(booking.overlaps(LocalTime.of(11, 0), LocalTime.of(13, 0)));
        assertFalse(booking.overlaps(LocalTime.of(12, 0), LocalTime.of(14, 0)));
        assertFalse(booking.overlaps(LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }

    /**
     * Pure overlap check: existing.start < requested.end AND existing.end > requested.start
     */
    private boolean overlapCheck(LocalTime existingStart, LocalTime existingEnd,
                                  LocalTime requestedStart, LocalTime requestedEnd) {
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }
}
