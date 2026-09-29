package com.workspace.management.dao.impl;

import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.MeetingRoomBookingDAO;
import com.workspace.management.model.MeetingRoomBooking;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeetingRoomBookingDAOImpl implements MeetingRoomBookingDAO {
    private static final Logger LOGGER = Logger.getLogger(MeetingRoomBookingDAOImpl.class.getName());
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    private static final String SELECT_ALL = """
        SELECT mrb.*, u.first_name, u.last_name, mr.room_name
        FROM meeting_room_bookings mrb
        JOIN users u ON mrb.user_id = u.id
        JOIN meeting_rooms mr ON mrb.room_id = mr.id
        """;

    private MeetingRoomBooking mapRow(ResultSet rs) throws SQLException {
        MeetingRoomBooking b = new MeetingRoomBooking();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setRoomId(rs.getInt("room_id"));
        b.setBookingDate(rs.getDate("booking_date").toLocalDate());
        b.setStartTime(rs.getTime("start_time").toLocalTime());
        b.setEndTime(rs.getTime("end_time").toLocalTime());
        b.setTitle(rs.getString("title"));
        b.setDescription(rs.getString("description"));
        b.setAttendeeCount(rs.getInt("attendee_count"));
        b.setStatus(rs.getString("status"));
        b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        try {
            b.setUserName(rs.getString("first_name") + " " + rs.getString("last_name"));
            b.setRoomName(rs.getString("room_name"));
        } catch (SQLException ignored) {}
        return b;
    }

    @Override
    public Optional<MeetingRoomBooking> findById(int id) {
        String sql = SELECT_ALL + " WHERE mrb.id = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public List<MeetingRoomBooking> findByUserId(int userId) {
        String sql = SELECT_ALL + " WHERE mrb.user_id = ? ORDER BY mrb.booking_date DESC, mrb.start_time";
        return executeListQuery(sql, ps -> ps.setInt(1, userId));
    }

    @Override
    public List<MeetingRoomBooking> findByRoomId(int roomId) {
        String sql = SELECT_ALL + " WHERE mrb.room_id = ? ORDER BY mrb.booking_date, mrb.start_time";
        return executeListQuery(sql, ps -> ps.setInt(1, roomId));
    }

    @Override
    public List<MeetingRoomBooking> findByDate(LocalDate date) {
        String sql = SELECT_ALL + " WHERE mrb.booking_date = ? ORDER BY mrb.start_time";
        return executeListQuery(sql, ps -> ps.setDate(1, Date.valueOf(date)));
    }

    @Override
    public List<MeetingRoomBooking> findAll() {
        String sql = SELECT_ALL + " ORDER BY mrb.booking_date DESC, mrb.start_time DESC";
        return executeListQuery(sql, ps -> {});
    }

    @Override
    public List<MeetingRoomBooking> findByRoomAndDate(int roomId, LocalDate date) {
        String sql = SELECT_ALL + " WHERE mrb.room_id = ? AND mrb.booking_date = ? AND mrb.status = 'CONFIRMED' ORDER BY mrb.start_time";
        return executeListQuery(sql, ps -> { ps.setInt(1, roomId); ps.setDate(2, Date.valueOf(date)); });
    }

    @Override
    public List<MeetingRoomBooking> findUpcomingByUserId(int userId) {
        String sql = SELECT_ALL + " WHERE mrb.user_id = ? AND mrb.status = 'CONFIRMED' AND (mrb.booking_date > CURDATE() OR (mrb.booking_date = CURDATE() AND mrb.end_time > CURTIME())) ORDER BY mrb.booking_date, mrb.start_time";
        return executeListQuery(sql, ps -> ps.setInt(1, userId));
    }

    @Override
    public boolean hasOverlappingBooking(Connection conn, int roomId, LocalDate date,
                                          LocalTime startTime, LocalTime endTime, int excludeBookingId) {
        String sql = """
            SELECT COUNT(*) FROM meeting_room_bookings
            WHERE room_id = ? AND booking_date = ?
            AND start_time < ? AND end_time > ?
            AND status = 'CONFIRMED'
            AND id != ?
            FOR UPDATE
            """;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(endTime));
            ps.setTime(4, Time.valueOf(startTime));
            ps.setInt(5, excludeBookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking room availability", e);
        }
        return false;
    }

    @Override
    public int create(Connection conn, MeetingRoomBooking booking) {
        String sql = "INSERT INTO meeting_room_bookings (user_id, room_id, booking_date, start_time, end_time, title, description, attendee_count, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getRoomId());
            ps.setDate(3, Date.valueOf(booking.getBookingDate()));
            ps.setTime(4, Time.valueOf(booking.getStartTime()));
            ps.setTime(5, Time.valueOf(booking.getEndTime()));
            ps.setString(6, booking.getTitle());
            ps.setString(7, booking.getDescription());
            ps.setInt(8, booking.getAttendeeCount());
            ps.setString(9, booking.getStatus().name());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating room booking", e);
        }
        return -1;
    }

    @Override
    public int create(MeetingRoomBooking booking) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            return create(conn, booking);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating room booking", e);
        } finally { dbConfig.releaseConnection(conn); }
        return -1;
    }

    @Override
    public boolean updateStatus(int bookingId, String status) {
        String sql = "UPDATE meeting_room_bookings SET status = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return false;
    }

    @Override
    public int countByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM meeting_room_bookings WHERE booking_date = ? AND status = 'CONFIRMED'";
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

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM meeting_room_bookings WHERE status = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return 0;
    }

    @FunctionalInterface
    private interface PreparedStatementSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<MeetingRoomBooking> executeListQuery(String sql, PreparedStatementSetter setter) {
        List<MeetingRoomBooking> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            setter.set(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing room booking query", e);
        } finally { dbConfig.releaseConnection(conn); }
        return list;
    }
}
