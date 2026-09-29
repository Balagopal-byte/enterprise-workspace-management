package com.workspace.management.dao.impl;

import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.EquipmentDAO;
import com.workspace.management.model.Equipment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EquipmentDAOImpl implements EquipmentDAO {
    private static final Logger LOGGER = Logger.getLogger(EquipmentDAOImpl.class.getName());
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    private Equipment mapRow(ResultSet rs) throws SQLException {
        Equipment eq = new Equipment();
        eq.setId(rs.getInt("id"));
        eq.setName(rs.getString("name"));
        eq.setCategory(rs.getString("category"));
        eq.setDescription(rs.getString("description"));
        eq.setStatus(rs.getString("status"));
        eq.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        try { eq.setQuantity(rs.getInt("quantity")); } catch (SQLException ignored) {}
        return eq;
    }

    @Override
    public Optional<Equipment> findById(int id) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM equipment WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public List<Equipment> findAll() {
        List<Equipment> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            ResultSet rs = conn.prepareStatement("SELECT * FROM equipment ORDER BY name").executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<Equipment> findByRoomId(int roomId) {
        return findByRoom(roomId);
    }

    @Override
    public List<Equipment> findActive() {
        List<Equipment> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            ResultSet rs = conn.prepareStatement("SELECT * FROM equipment WHERE status='ACTIVE' ORDER BY name").executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return list;
    }

    @Override
    public int create(Equipment equipment) {
        String sql = "INSERT INTO equipment (name, category, description, status) VALUES (?,?,?,?)";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, equipment.getName());
            ps.setString(2, equipment.getCategory());
            ps.setString(3, equipment.getDescription());
            ps.setString(4, equipment.getStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return -1;
    }

    @Override
    public boolean update(Equipment equipment) {
        String sql = "UPDATE equipment SET name=?, category=?, description=?, status=? WHERE id=?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, equipment.getName());
            ps.setString(2, equipment.getCategory());
            ps.setString(3, equipment.getDescription());
            ps.setString(4, equipment.getStatus());
            ps.setInt(5, equipment.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return false;
    }

    @Override
    public boolean updateStatus(int equipmentId, String status) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE equipment SET status=? WHERE id=?");
            ps.setString(1, status);
            ps.setInt(2, equipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return false;
    }

    @Override
    public boolean addToRoom(int roomId, int equipmentId, int quantity) {
        String sql = "INSERT INTO room_equipment (room_id, equipment_id, quantity) VALUES (?,?,?) ON DUPLICATE KEY UPDATE quantity=?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);
            ps.setInt(2, equipmentId);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return false;
    }

    @Override
    public boolean removeFromRoom(int roomId, int equipmentId) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM room_equipment WHERE room_id=? AND equipment_id=?");
            ps.setInt(1, roomId);
            ps.setInt(2, equipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return false;
    }

    @Override
    public List<Equipment> findByRoom(int roomId) {
        String sql = "SELECT e.*, re.quantity FROM equipment e JOIN room_equipment re ON e.id = re.equipment_id WHERE re.room_id = ? ORDER BY e.name";
        List<Equipment> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
        finally { dbConfig.releaseConnection(conn); }
        return list;
    }
}
