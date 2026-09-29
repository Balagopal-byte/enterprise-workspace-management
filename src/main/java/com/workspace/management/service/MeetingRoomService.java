package com.workspace.management.service;

import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.MeetingRoomBookingDAO;
import com.workspace.management.dao.AnalyticsDAO;
import com.workspace.management.dao.impl.MeetingRoomBookingDAOImpl;
import com.workspace.management.dao.impl.AnalyticsDAOImpl;
import com.workspace.management.exception.BookingConflictException;
import com.workspace.management.model.MeetingRoomBooking;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for meeting room booking with transaction-based concurrency control.
 * Uses the same pessimistic locking strategy as BookingService.
 */
public class MeetingRoomService {
    private static final Logger LOGGER = Logger.getLogger(MeetingRoomService.class.getName());
    private final MeetingRoomBookingDAO roomBookingDAO;
    private final AnalyticsDAO analyticsDAO;
    private final DatabaseConfig dbConfig;

    public MeetingRoomService() {
        this.roomBookingDAO = new MeetingRoomBookingDAOImpl();
        this.analyticsDAO = new AnalyticsDAOImpl();
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /**
     * Create a meeting room booking with concurrency protection.
     */
    public MeetingRoomBooking createBooking(int userId, int roomId, LocalDate date,
                                             LocalTime startTime, LocalTime endTime,
                                             String title, String description, int attendeeCount)
            throws BookingConflictException {

        if (title == null || title.trim().isEmpty()) {
            throw new BookingConflictException("Meeting title is required.");
        }
        if (date == null || startTime == null || endTime == null) {
            throw new BookingConflictException("Date, start time, and end time are required.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BookingConflictException("End time must be after start time.");
        }

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            boolean hasConflict = roomBookingDAO.hasOverlappingBooking(
                    conn, roomId, date, startTime, endTime, 0);

            if (hasConflict) {
                conn.rollback();
                throw new BookingConflictException(
                        "This room is already booked for the selected time. Please choose another time slot.");
            }

            MeetingRoomBooking booking = new MeetingRoomBooking(userId, roomId, date, startTime, endTime, title);
            booking.setDescription(description);
            booking.setAttendeeCount(attendeeCount);
            int bookingId = roomBookingDAO.create(conn, booking);
            booking.setId(bookingId);

            conn.commit();

            analyticsDAO.logAudit(userId, "ROOM_BOOKING_CREATED", "MEETING_ROOM_BOOKING", bookingId,
                    "Room booking '" + title + "' created for " + date);

            return booking;

        } catch (BookingConflictException e) {
            throw e;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback error", ex); }
            }
            throw new BookingConflictException("Unable to create room booking. Please try again.");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                dbConfig.releaseConnection(conn);
            }
        }
    }

    public boolean cancelBooking(int bookingId, int userId) {
        boolean result = roomBookingDAO.updateStatus(bookingId, "CANCELLED");
        if (result) {
            analyticsDAO.logAudit(userId, "ROOM_BOOKING_CANCELLED", "MEETING_ROOM_BOOKING", bookingId, "Room booking cancelled");
        }
        return result;
    }

    public List<MeetingRoomBooking> getUserBookings(int userId) {
        return roomBookingDAO.findByUserId(userId);
    }

    public List<MeetingRoomBooking> getUpcomingBookings(int userId) {
        return roomBookingDAO.findUpcomingByUserId(userId);
    }

    public List<MeetingRoomBooking> getRoomBookings(int roomId, LocalDate date) {
        return roomBookingDAO.findByRoomAndDate(roomId, date);
    }

    public List<MeetingRoomBooking> getAllBookings() {
        return roomBookingDAO.findAll();
    }

    public List<MeetingRoomBooking> getBookingsByDate(LocalDate date) {
        return roomBookingDAO.findByDate(date);
    }
}
