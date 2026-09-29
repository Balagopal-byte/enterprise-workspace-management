package com.workspace.management.dao.impl;

import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.BookingDAO;
import com.workspace.management.model.Booking;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of BookingDAO with transaction support.
 * Critical methods accept Connection parameter for use within transactions.
 */
public class BookingDAOImpl implements BookingDAO {
    private static final Logger LOGGER = Logger.getLogger(BookingDAOImpl.class.getName());
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    private static final String SELECT_ALL = """
        SELECT b.*, u.first_name, u.last_name, u.email, d.desk_code, d.zone
        FROM bookings b
        JOIN users u ON b.user_id = u.id
        JOIN desks d ON b.desk_id = d.id
        """;

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setDeskId(rs.getInt("desk_id"));
        b.setBookingDate(rs.getDate("booking_date").toLocalDate());
        b.setStartTime(rs.getTime("start_time").toLocalTime());
        b.setEndTime(rs.getTime("end_time").toLocalTime());
        b.setStatus(rs.getString("status"));
        b.setNotes(rs.getString("notes"));
        b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) b.setUpdatedAt(updatedAt.toLocalDateTime());
        try {
            b.setUserName(rs.getString("first_name") + " " + rs.getString("last_name"));
            b.setUserEmail(rs.getString("email"));
            b.setDeskCode(rs.getString("desk_code"));
            b.setDeskZone(rs.getString("zone"));
        } catch (SQLException ignored) {} // Not all queries join users/desks
        return b;
    }

    @Override
    public Optional<Booking> findById(int id) {
        String sql = SELECT_ALL + " WHERE b.id = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding booking by id", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<Booking> findByUserId(int userId) {
        String sql = SELECT_ALL + " WHERE b.user_id = ? ORDER BY b.booking_date DESC, b.start_time DESC";
        return findByQuery(sql, userId);
    }

    @Override
    public List<Booking> findByDeskId(int deskId) {
        String sql = SELECT_ALL + " WHERE b.desk_id = ? ORDER BY b.booking_date DESC";
        return findByQuery(sql, deskId);
    }

    @Override
    public List<Booking> findByDate(LocalDate date) {
        String sql = SELECT_ALL + " WHERE b.booking_date = ? ORDER BY b.start_time";
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding bookings by date", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return bookings;
    }

    @Override
    public List<Booking> findByUserIdAndStatus(int userId, String status) {
        String sql = SELECT_ALL + " WHERE b.user_id = ? AND b.status = ? ORDER BY b.booking_date, b.start_time";
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding bookings by user and status", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return bookings;
    }

    @Override
    public List<Booking> findByStatus(String status) {
        String sql = SELECT_ALL + " WHERE b.status = ? ORDER BY b.booking_date, b.start_time";
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding bookings by status", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return bookings;
    }

    @Override
    public List<Booking> findAll() {
        String sql = SELECT_ALL + " ORDER BY b.booking_date DESC, b.start_time DESC";
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all bookings", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return bookings;
    }

    @Override
    public List<Booking> findUpcomingByUserId(int userId) {
        String sql = SELECT_ALL + " WHERE b.user_id = ? AND b.status IN ('CONFIRMED','PENDING') AND (b.booking_date > CURDATE() OR (b.booking_date = CURDATE() AND b.end_time > CURTIME())) ORDER BY b.booking_date, b.start_time";
        return findByQuery(sql, userId);
    }

    @Override
    public List<Booking> findActiveByUserId(int userId) {
        String sql = SELECT_ALL + " WHERE b.user_id = ? AND b.status IN ('CONFIRMED','CHECKED_IN') AND b.booking_date = CURDATE() AND b.start_time <= CURTIME() AND b.end_time >= CURTIME() ORDER BY b.start_time";
        return findByQuery(sql, userId);
    }

    @Override
    public List<Booking> findTodaysBookings() {
        String sql = SELECT_ALL + " WHERE b.booking_date = CURDATE() ORDER BY b.start_time";
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding today's bookings", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return bookings;
    }

    /**
     * Check for overlapping bookings using proper interval overlap detection.
     * existing.start < requested.end AND existing.end > requested.start
     * Uses SELECT ... FOR UPDATE for pessimistic locking within a transaction.
     */
    @Override
    public boolean hasOverlappingBooking(Connection conn, int deskId, LocalDate date,
                                          LocalTime startTime, LocalTime endTime, int excludeBookingId) {
        String sql = """
            SELECT COUNT(*) FROM bookings
            WHERE desk_id = ? AND booking_date = ?
            AND start_time < ? AND end_time > ?
            AND status IN ('CONFIRMED', 'CHECKED_IN', 'PENDING')
            AND id != ?
            FOR UPDATE
            """;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, deskId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(endTime));
            ps.setTime(4, Time.valueOf(startTime));
            ps.setInt(5, excludeBookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking overlapping bookings", e);
            throw new RuntimeException("Error checking availability", e);
        }
        return false;
    }

    /**
     * Create a booking within a transaction (no auto-commit).
     */
    @Override
    public int create(Connection conn, Booking booking) {
        String sql = "INSERT INTO bookings (user_id, desk_id, booking_date, start_time, end_time, status, notes) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getDeskId());
            ps.setDate(3, Date.valueOf(booking.getBookingDate()));
            ps.setTime(4, Time.valueOf(booking.getStartTime()));
            ps.setTime(5, Time.valueOf(booking.getEndTime()));
            ps.setString(6, booking.getStatus().name());
            ps.setString(7, booking.getNotes());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating booking in transaction", e);
            throw new RuntimeException("Error creating booking", e);
        }
        return -1;
    }

    @Override
    public int create(Booking booking) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            return create(conn, booking);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating booking", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return -1;
    }

    @Override
    public boolean updateStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating booking status", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return false;
    }

    @Override
    public boolean updateStatus(Connection conn, int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating booking status in transaction", e);
        }
    }

    @Override
    public int countByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE booking_date = ? AND status IN ('CONFIRMED','CHECKED_IN','PENDING')";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting bookings by date", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return 0;
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting bookings by status", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return 0;
    }

    @Override
    public int countByDateRange(LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE booking_date BETWEEN ? AND ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting bookings by date range", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return 0;
    }

    /**
     * Find bookings that are past the grace period and not checked in.
     * These should be auto-released.
     */
    @Override
    public List<Booking> findBookingsForAutoRelease(int gracePeriodMinutes) {
        String sql = """
            SELECT b.*, u.first_name, u.last_name, u.email, d.desk_code, d.zone
            FROM bookings b
            JOIN users u ON b.user_id = u.id
            JOIN desks d ON b.desk_id = d.id
            WHERE b.status = 'CONFIRMED'
            AND b.booking_date = CURDATE()
            AND ADDTIME(b.start_time, SEC_TO_TIME(? * 60)) < CURTIME()
            AND b.id NOT IN (SELECT booking_id FROM check_ins)
            ORDER BY b.start_time
            """;
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, gracePeriodMinutes);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding bookings for auto-release", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return bookings;
    }

    private List<Booking> findByQuery(String sql, int paramValue) {
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, paramValue);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing booking query", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return bookings;
    }
}
