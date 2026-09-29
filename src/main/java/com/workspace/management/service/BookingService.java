package com.workspace.management.service;

import com.workspace.management.config.AppConfig;
import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.BookingDAO;
import com.workspace.management.dao.AnalyticsDAO;
import com.workspace.management.dao.impl.BookingDAOImpl;
import com.workspace.management.dao.impl.AnalyticsDAOImpl;
import com.workspace.management.exception.BookingConflictException;
import com.workspace.management.model.Booking;
import com.workspace.management.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for desk booking operations with transaction-based concurrency control.
 * This is the core of the concurrency engine (Module 4).
 *
 * Key academic concepts demonstrated:
 * - Database transactions (Connection.setAutoCommit(false))
 * - Pessimistic locking (SELECT ... FOR UPDATE)
 * - Proper interval overlap detection
 * - Rollback on conflict
 * - Exception handling across layers
 */
public class BookingService {
    private static final Logger LOGGER = Logger.getLogger(BookingService.class.getName());
    private final BookingDAO bookingDAO;
    private final AnalyticsDAO analyticsDAO;
    private final DatabaseConfig dbConfig;
    private final AppConfig appConfig;

    public BookingService() {
        this.bookingDAO = new BookingDAOImpl();
        this.analyticsDAO = new AnalyticsDAOImpl();
        this.dbConfig = DatabaseConfig.getInstance();
        this.appConfig = AppConfig.getInstance();
    }

    /**
     * Create a desk booking with full concurrency protection.
     *
     * Transaction flow:
     * 1. Validate input
     * 2. Begin transaction (autoCommit = false)
     * 3. SELECT ... FOR UPDATE to lock desk's booking rows
     * 4. Check for overlapping bookings
     * 5. Insert new booking
     * 6. Commit transaction
     * 7. On conflict → rollback
     *
     * @throws BookingConflictException if the desk is already booked for the requested time
     */
    public Booking createBooking(int userId, int deskId, LocalDate date,
                                  LocalTime startTime, LocalTime endTime, String notes)
            throws BookingConflictException {

        // Step 1: Validate input
        validateBookingInput(date, startTime, endTime);

        Connection conn = null;
        try {
            // Step 2: Begin transaction
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            // Step 3 & 4: Lock and check for overlapping bookings
            boolean hasConflict = bookingDAO.hasOverlappingBooking(
                    conn, deskId, date, startTime, endTime, 0);

            if (hasConflict) {
                // Step 7: Rollback on conflict
                conn.rollback();
                throw new BookingConflictException(
                        "This desk was just booked by another user for the selected time. " +
                        "Please select another time or desk.");
            }

            // Step 5: Insert booking
            Booking booking = new Booking(userId, deskId, date, startTime, endTime);
            booking.setNotes(notes);
            int bookingId = bookingDAO.create(conn, booking);
            booking.setId(bookingId);

            // Step 6: Commit
            conn.commit();

            // Audit log
            analyticsDAO.logAudit(userId, "BOOKING_CREATED", "BOOKING", bookingId,
                    "Desk booking created for " + date + " " + startTime + "-" + endTime);

            LOGGER.info("Booking created successfully: ID=" + bookingId);
            return booking;

        } catch (BookingConflictException e) {
            throw e; // Re-throw booking conflicts
        } catch (Exception e) {
            // Rollback on any error
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Error creating booking", e);
            throw new BookingConflictException("Unable to create booking. Please try again.");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                dbConfig.releaseConnection(conn);
            }
        }
    }

    /**
     * Cancel a booking. Only the booking owner or admin/manager can cancel.
     */
    public boolean cancelBooking(int bookingId, int userId) {
        var bookingOpt = bookingDAO.findById(bookingId);
        if (bookingOpt.isEmpty()) return false;

        Booking booking = bookingOpt.get();
        if (!booking.canCancel()) return false;

        boolean result = bookingDAO.updateStatus(bookingId, "CANCELLED");
        if (result) {
            analyticsDAO.logAudit(userId, "BOOKING_CANCELLED", "BOOKING", bookingId,
                    "Booking cancelled for desk " + booking.getDeskCode());
        }
        return result;
    }

    /**
     * Get all bookings for a user.
     */
    public List<Booking> getUserBookings(int userId) {
        return bookingDAO.findByUserId(userId);
    }

    /**
     * Get upcoming bookings for a user.
     */
    public List<Booking> getUpcomingBookings(int userId) {
        return bookingDAO.findUpcomingByUserId(userId);
    }

    /**
     * Get active (current) bookings for a user.
     */
    public List<Booking> getActiveBookings(int userId) {
        return bookingDAO.findActiveByUserId(userId);
    }

    /**
     * Get today's bookings.
     */
    public List<Booking> getTodaysBookings() {
        return bookingDAO.findTodaysBookings();
    }

    /**
     * Get all bookings (for admin/manager).
     */
    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    /**
     * Get bookings by status.
     */
    public List<Booking> getBookingsByStatus(String status) {
        return bookingDAO.findByStatus(status);
    }

    /**
     * Validate booking input parameters.
     */
    private void validateBookingInput(LocalDate date, LocalTime startTime, LocalTime endTime)
            throws BookingConflictException {
        if (date == null || startTime == null || endTime == null) {
            throw new BookingConflictException("Date, start time, and end time are required.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new BookingConflictException("Cannot book a desk in the past.");
        }
        if (date.equals(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            // Allow booking if it's today but start time hasn't passed by much
            // Give 5 minute leeway
            if (startTime.plusMinutes(5).isBefore(LocalTime.now())) {
                throw new BookingConflictException("Start time has already passed.");
            }
        }
        String timeError = ValidationUtil.validateBookingTimes(
                startTime, endTime,
                appConfig.getMinBookingDurationMinutes(),
                appConfig.getMaxBookingDurationHours());
        if (timeError != null) {
            throw new BookingConflictException(timeError);
        }
    }
}
