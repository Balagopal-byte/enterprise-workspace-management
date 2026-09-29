package com.workspace.management.dao.impl;

import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.MeetingRoomDAO;
import com.workspace.management.model.MeetingRoom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeetingRoomDAOImpl implements MeetingRoomDAO {
    private static final Logger LOGGER = Logger.getLogger(MeetingRoomDAOImpl.class.getName());
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    private MeetingRoom mapRow(ResultSet rs) throws SQLException {
        MeetingRoom room = new MeetingRoom();
        room.setId(rs.getInt("id"));
        room.setRoomName(rs.getString("room_name"));
        room.setFloor(rs.getInt("floor"));
        room.setCapacity(rs.getInt("capacity"));
        room.setLocation(rs.getString("location"));
        room.setStatus(rs.getString("status"));
        room.setDescription(rs.getString("description"));
        room.setPositionX(rs.getInt("position_x"));
        room.setPositionY(rs.getInt("position_y"));
        room.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) room.setUpdatedAt(updatedAt.toLocalDateTime());
        return room;
    }

    @Override
    public Optional<MeetingRoom> findById(int id) {
        String sql = "SELECT * FROM meeting_rooms WHERE id = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding room by id", e);
        } finally { dbConfig.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public List<MeetingRoom> findAll() {
        String sql = "SELECT * FROM meeting_rooms ORDER BY room_name";
        List<MeetingRoom> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all rooms", e);
        } finally { dbConfig.releaseConnection(conn); }
        return rooms;
    }

    @Override
    public List<MeetingRoom> findByStatus(String status) {
        String sql = "SELECT * FROM meeting_rooms WHERE status = ? ORDER BY room_name";
        List<MeetingRoom> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding rooms by status", e);
        } finally { dbConfig.releaseConnection(conn); }
        return rooms;
    }

    @Override
    public List<MeetingRoom> findByFloor(int floor) {
        String sql = "SELECT * FROM meeting_rooms WHERE floor = ? ORDER BY room_name";
        List<MeetingRoom> rooms = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, floor);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding rooms by floor", e);
        } finally { dbConfig.releaseConnection(conn); }
        return rooms;
    }

    @Override
    public int create(MeetingRoom room) {
        String sql = "INSERT INTO meeting_rooms (room_name, floor, capacity, location, status, description, position_x, position_y) VALUES (?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, room.getRoomName());
            ps.setInt(2, room.getFloor());
            ps.setInt(3, room.getCapacity());
            ps.setString(4, room.getLocation());
            ps.setString(5, room.getStatus().name());
            ps.setString(6, room.getDescription());
            ps.setInt(7, room.getPositionX());
            ps.setInt(8, room.getPositionY());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating room", e);
        } finally { dbConfig.releaseConnection(conn); }
        return -1;
    }

    @Override
    public boolean update(MeetingRoom room) {
        String sql = "UPDATE meeting_rooms SET room_name=?, floor=?, capacity=?, location=?, status=?, description=?, position_x=?, position_y=? WHERE id=?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, room.getRoomName());
            ps.setInt(2, room.getFloor());
            ps.setInt(3, room.getCapacity());
            ps.setString(4, room.getLocation());
            ps.setString(5, room.getStatus().name());
            ps.setString(6, room.getDescription());
            ps.setInt(7, room.getPositionX());
            ps.setInt(8, room.getPositionY());
            ps.setInt(9, room.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room", e);
        } finally { dbConfig.releaseConnection(conn); }
        return false;
    }

    @Override
    public boolean updateStatus(int roomId, String status) {
        String sql = "UPDATE meeting_rooms SET status = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room status", e);
        } finally { dbConfig.releaseConnection(conn); }
        return false;
    }

    @Override
    public int countAll() {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM meeting_rooms WHERE status != 'INACTIVE'");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return 0;
    }

    @Override
    public int countByStatus(String status) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM meeting_rooms WHERE status = ?");
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return 0;
    }
}
