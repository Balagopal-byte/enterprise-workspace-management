package com.workspace.management.service;

import com.workspace.management.config.AppConfig;
import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.BookingDAO;
import com.workspace.management.dao.AnalyticsDAO;
import com.workspace.management.dao.impl.BookingDAOImpl;
import com.workspace.management.dao.impl.AnalyticsDAOImpl;
import com.workspace.management.model.Booking;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for check-in operations.
 * Validates check-in within the configurable grace period window.
 */
public class CheckInService {
    private static final Logger LOGGER = Logger.getLogger(CheckInService.class.getName());
    private final BookingDAO bookingDAO;
    private final AnalyticsDAO analyticsDAO;
    private final DatabaseConfig dbConfig;
    private final int gracePeriodMinutes;

    public CheckInService() {
        this.bookingDAO = new BookingDAOImpl();
        this.analyticsDAO = new AnalyticsDAOImpl();
        this.dbConfig = DatabaseConfig.getInstance();
        this.gracePeriodMinutes = AppConfig.getInstance().getCheckinGracePeriodMinutes();
    }

    /**
     * Check in to a booking.
     * Only allowed within the grace period window: 15 min before to grace period after start.
     */
    public boolean checkIn(int bookingId, int userId) {
        Optional<Booking> bookingOpt = bookingDAO.findById(bookingId);
        if (bookingOpt.isEmpty()) return false;

        Booking booking = bookingOpt.get();

        // Verify booking belongs to user
        if (booking.getUserId() != userId) return false;

        // Verify booking is in CONFIRMED status
        if (!booking.canCheckIn()) return false;

        // Verify booking is for today
        if (!booking.getBookingDate().equals(LocalDate.now())) return false;

        // Check time window
        LocalTime now = LocalTime.now();
        LocalTime earliestCheckIn = booking.getStartTime().minusMinutes(gracePeriodMinutes);
        LocalTime latestCheckIn = booking.getEndTime();

        if (now.isBefore(earliestCheckIn)) {
            LOGGER.info("Check-in too early for booking " + bookingId);
            return false;
        }
        if (now.isAfter(latestCheckIn)) {
            LOGGER.info("Check-in too late for booking " + bookingId);
            return false;
        }

        // Perform check-in in a transaction
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            // Update booking status
            bookingDAO.updateStatus(conn, bookingId, "CHECKED_IN");

            // Insert check-in record
            String sql = "INSERT INTO check_ins (booking_id, user_id, check_in_time) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, bookingId);
            ps.setInt(2, userId);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            conn.commit();

            analyticsDAO.logAudit(userId, "CHECK_IN", "BOOKING", bookingId,
                    "User checked in to booking " + bookingId);

            LOGGER.info("Check-in successful for booking " + bookingId);
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback error", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error during check-in", e);
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                dbConfig.releaseConnection(conn);
            }
        }
    }

    /**
     * Get bookings eligible for check-in for the current user.
     */
    public List<Booking> getCheckInEligibleBookings(int userId) {
        return bookingDAO.findActiveByUserId(userId);
    }

    /**
     * Get all confirmed bookings for today for a user (including upcoming).
     */
    public List<Booking> getTodaysConfirmedBookings(int userId) {
        return bookingDAO.findByUserIdAndStatus(userId, "CONFIRMED");
    }

    /**
     * Check if a booking is within the check-in window.
     */
    public boolean isWithinCheckInWindow(Booking booking) {
        if (!booking.getBookingDate().equals(LocalDate.now())) return false;
        LocalTime now = LocalTime.now();
        LocalTime earliest = booking.getStartTime().minusMinutes(gracePeriodMinutes);
        return !now.isBefore(earliest) && !now.isAfter(booking.getEndTime());
    }

    public int getGracePeriodMinutes() {
        return gracePeriodMinutes;
    }
}
