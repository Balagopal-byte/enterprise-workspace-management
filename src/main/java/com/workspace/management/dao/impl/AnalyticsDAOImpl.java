package com.workspace.management.dao.impl;

import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.AnalyticsDAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AnalyticsDAO implementation that queries real data from the database.
 * All dashboard statistics come from actual database records.
 */
public class AnalyticsDAOImpl implements AnalyticsDAO {
    private static final Logger LOGGER = Logger.getLogger(AnalyticsDAOImpl.class.getName());
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    @Override
    public int getTotalDesks() { return singleInt("SELECT COUNT(*) FROM desks WHERE status != 'INACTIVE'"); }

    @Override
    public int getAvailableDesks() {
        // Available = not currently booked and not maintenance/inactive
        String sql = """
            SELECT COUNT(*) FROM desks d WHERE d.status NOT IN ('MAINTENANCE','INACTIVE')
            AND d.id NOT IN (
                SELECT desk_id FROM bookings
                WHERE booking_date = CURDATE()
                AND status IN ('CONFIRMED','CHECKED_IN')
                AND start_time <= CURTIME() AND end_time > CURTIME()
            )
            """;
        return singleInt(sql);
    }

    @Override
    public int getOccupiedDesks() {
        String sql = """
            SELECT COUNT(DISTINCT desk_id) FROM bookings
            WHERE booking_date = CURDATE()
            AND status IN ('CONFIRMED','CHECKED_IN')
            AND start_time <= CURTIME() AND end_time > CURTIME()
            """;
        return singleInt(sql);
    }

    @Override
    public int getMaintenanceDesks() { return singleInt("SELECT COUNT(*) FROM desks WHERE status = 'MAINTENANCE'"); }

    @Override
    public int getTotalRooms() { return singleInt("SELECT COUNT(*) FROM meeting_rooms WHERE status != 'INACTIVE'"); }

    @Override
    public int getAvailableRooms() {
        String sql = """
            SELECT COUNT(*) FROM meeting_rooms mr WHERE mr.status NOT IN ('MAINTENANCE','INACTIVE')
            AND mr.id NOT IN (
                SELECT room_id FROM meeting_room_bookings
                WHERE booking_date = CURDATE()
                AND status = 'CONFIRMED'
                AND start_time <= CURTIME() AND end_time > CURTIME()
            )
            """;
        return singleInt(sql);
    }

    @Override
    public int getTodayBookingCount() {
        return singleIntParam("SELECT COUNT(*) FROM bookings WHERE booking_date = ? AND status IN ('CONFIRMED','CHECKED_IN','PENDING')", LocalDate.now());
    }

    @Override
    public int getTodayRoomBookingCount() {
        return singleIntParam("SELECT COUNT(*) FROM meeting_room_bookings WHERE booking_date = ? AND status = 'CONFIRMED'", LocalDate.now());
    }

    @Override
    public double getCurrentOccupancyPercentage() {
        int total = getTotalDesks();
        if (total == 0) return 0;
        int occupied = getOccupiedDesks();
        return (occupied * 100.0) / total;
    }

    @Override
    public Map<String, Integer> getDeskUtilizationByZone(LocalDate start, LocalDate end) {
        String sql = """
            SELECT d.zone, COUNT(b.id) as booking_count
            FROM desks d LEFT JOIN bookings b ON d.id = b.desk_id
            AND b.booking_date BETWEEN ? AND ?
            AND b.status IN ('CONFIRMED','CHECKED_IN','COMPLETED')
            GROUP BY d.zone ORDER BY booking_count DESC
            """;
        Map<String, Integer> map = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("zone"), rs.getInt("booking_count"));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return map;
    }

    @Override
    public Map<Integer, Integer> getOccupancyByHour(LocalDate date) {
        String sql = """
            SELECT HOUR(start_time) as hour, COUNT(*) as count
            FROM bookings WHERE booking_date = ?
            AND status IN ('CONFIRMED','CHECKED_IN','COMPLETED')
            GROUP BY HOUR(start_time) ORDER BY hour
            """;
        Map<Integer, Integer> map = new LinkedHashMap<>();
        // Initialize all hours
        for (int h = 7; h <= 20; h++) map.put(h, 0);
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getInt("hour"), rs.getInt("count"));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return map;
    }

    @Override
    public Map<String, Integer> getBookingsByStatus(LocalDate start, LocalDate end) {
        String sql = "SELECT status, COUNT(*) as count FROM bookings WHERE booking_date BETWEEN ? AND ? GROUP BY status";
        Map<String, Integer> map = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("status"), rs.getInt("count"));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return map;
    }

    @Override
    public Map<LocalDate, Integer> getBookingTrend(LocalDate start, LocalDate end) {
        String sql = "SELECT booking_date, COUNT(*) as count FROM bookings WHERE booking_date BETWEEN ? AND ? GROUP BY booking_date ORDER BY booking_date";
        Map<LocalDate, Integer> map = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getDate("booking_date").toLocalDate(), rs.getInt("count"));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return map;
    }

    @Override
    public Map<String, Integer> getRoomUtilization(LocalDate start, LocalDate end) {
        String sql = """
            SELECT mr.room_name, COUNT(mrb.id) as booking_count
            FROM meeting_rooms mr LEFT JOIN meeting_room_bookings mrb ON mr.id = mrb.room_id
            AND mrb.booking_date BETWEEN ? AND ?
            AND mrb.status IN ('CONFIRMED','COMPLETED')
            GROUP BY mr.room_name ORDER BY booking_count DESC
            """;
        Map<String, Integer> map = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("room_name"), rs.getInt("booking_count"));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return map;
    }

    @Override
    public int getBookingCount(LocalDate start, LocalDate end) {
        return singleIntRange("SELECT COUNT(*) FROM bookings WHERE booking_date BETWEEN ? AND ?", start, end);
    }

    @Override
    public int getCancellationCount(LocalDate start, LocalDate end) {
        return singleIntRange("SELECT COUNT(*) FROM bookings WHERE booking_date BETWEEN ? AND ? AND status='CANCELLED'", start, end);
    }

    @Override
    public int getAutoReleaseCount(LocalDate start, LocalDate end) {
        return singleIntRange("SELECT COUNT(*) FROM bookings WHERE booking_date BETWEEN ? AND ? AND status='AUTO_RELEASED'", start, end);
    }

    @Override
    public int getCheckInCount(LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(*) FROM check_ins c JOIN bookings b ON c.booking_id = b.id WHERE b.booking_date BETWEEN ? AND ?";
        return singleIntRange(sql, start, end);
    }

    @Override
    public double getCheckInRate(LocalDate start, LocalDate end) {
        int total = getBookingCount(start, end);
        if (total == 0) return 0;
        int checkedIn = getCheckInCount(start, end);
        return (checkedIn * 100.0) / total;
    }

    @Override
    public double getAverageOccupancy(LocalDate start, LocalDate end) {
        return getCurrentOccupancyPercentage(); // Simplified
    }

    @Override
    public double getPeakOccupancy(LocalDate start, LocalDate end) {
        String sql = """
            SELECT MAX(cnt) FROM (
                SELECT COUNT(*) as cnt FROM bookings
                WHERE booking_date BETWEEN ? AND ?
                AND status IN ('CONFIRMED','CHECKED_IN','COMPLETED')
                GROUP BY booking_date, HOUR(start_time)
            ) sub
            """;
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int peak = rs.getInt(1);
                int total = getTotalDesks();
                return total > 0 ? (peak * 100.0) / total : 0;
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return 0;
    }

    @Override
    public void logAudit(int userId, String action, String entityType, Integer entityId, String description) {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, description) VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            if (userId > 0) ps.setInt(1, userId); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId != null) ps.setInt(4, entityId); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error logging audit", e);
        } finally { dbConfig.releaseConnection(conn); }
    }

    // --- Helper methods ---
    private int singleInt(String sql) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error in count query", e); }
        finally { dbConfig.releaseConnection(conn); }
        return 0;
    }

    private int singleIntParam(String sql, LocalDate date) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return 0;
    }

    private int singleIntRange(String sql, LocalDate start, LocalDate end) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return 0;
    }
}
