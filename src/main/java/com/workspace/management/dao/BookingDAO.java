package com.workspace.management.dao;

import com.workspace.management.model.Booking;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Desk Booking operations.
 * Includes methods that accept Connection for transaction support.
 */
public interface BookingDAO {
    Optional<Booking> findById(int id);
    List<Booking> findByUserId(int userId);
    List<Booking> findByDeskId(int deskId);
    List<Booking> findByDate(LocalDate date);
    List<Booking> findByUserIdAndStatus(int userId, String status);
    List<Booking> findByStatus(String status);
    List<Booking> findAll();
    List<Booking> findUpcomingByUserId(int userId);
    List<Booking> findActiveByUserId(int userId);
    List<Booking> findTodaysBookings();

    /**
     * Check if a desk has any overlapping bookings for the given time.
     * Used within transactions.
     */
    boolean hasOverlappingBooking(Connection conn, int deskId, LocalDate date,
                                   LocalTime startTime, LocalTime endTime, int excludeBookingId);

    /**
     * Create a booking within a transaction.
     */
    int create(Connection conn, Booking booking);

    int create(Booking booking);
    boolean updateStatus(int bookingId, String status);
    boolean updateStatus(Connection conn, int bookingId, String status);

    int countByDate(LocalDate date);
    int countByStatus(String status);
    int countByDateRange(LocalDate start, LocalDate end);

    /**
     * Find bookings that should be auto-released (past grace period, not checked in).
     */
    List<Booking> findBookingsForAutoRelease(int gracePeriodMinutes);
}
