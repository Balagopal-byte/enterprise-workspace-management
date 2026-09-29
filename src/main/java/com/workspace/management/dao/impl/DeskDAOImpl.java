package com.workspace.management.dao.impl;

import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.DeskDAO;
import com.workspace.management.model.Desk;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of DeskDAO.
 */
public class DeskDAOImpl implements DeskDAO {
    private static final Logger LOGGER = Logger.getLogger(DeskDAOImpl.class.getName());
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    private Desk mapRow(ResultSet rs) throws SQLException {
        Desk desk = new Desk();
        desk.setId(rs.getInt("id"));
        desk.setDeskCode(rs.getString("desk_code"));
        desk.setFloor(rs.getInt("floor"));
        desk.setZone(rs.getString("zone"));
        desk.setPositionX(rs.getInt("position_x"));
        desk.setPositionY(rs.getInt("position_y"));
        desk.setDeskType(rs.getString("desk_type"));
        desk.setStatus(rs.getString("status"));
        desk.setHasMonitor(rs.getBoolean("has_monitor"));
        desk.setHasPhone(rs.getBoolean("has_phone"));
        desk.setHasPowerOutlet(rs.getBoolean("has_power_outlet"));
        desk.setDescription(rs.getString("description"));
        desk.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) desk.setUpdatedAt(updatedAt.toLocalDateTime());
        return desk;
    }

    @Override
    public Optional<Desk> findById(int id) {
        String sql = "SELECT * FROM desks WHERE id = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding desk by id", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Desk> findByCode(String code) {
        String sql = "SELECT * FROM desks WHERE desk_code = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding desk by code", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<Desk> findAll() {
        String sql = "SELECT * FROM desks ORDER BY desk_code";
        List<Desk> desks = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) desks.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all desks", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return desks;
    }

    @Override
    public List<Desk> findByFloor(int floor) {
        String sql = "SELECT * FROM desks WHERE floor = ? ORDER BY desk_code";
        List<Desk> desks = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, floor);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) desks.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding desks by floor", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return desks;
    }

    @Override
    public List<Desk> findByZone(String zone) {
        String sql = "SELECT * FROM desks WHERE zone = ? ORDER BY desk_code";
        List<Desk> desks = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, zone);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) desks.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding desks by zone", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return desks;
    }

    @Override
    public List<Desk> findByStatus(String status) {
        String sql = "SELECT * FROM desks WHERE status = ? ORDER BY desk_code";
        List<Desk> desks = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) desks.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding desks by status", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return desks;
    }

    @Override
    public int create(Desk desk) {
        String sql = "INSERT INTO desks (desk_code, floor, zone, position_x, position_y, desk_type, status, has_monitor, has_phone, has_power_outlet, description) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, desk.getDeskCode());
            ps.setInt(2, desk.getFloor());
            ps.setString(3, desk.getZone());
            ps.setInt(4, desk.getPositionX());
            ps.setInt(5, desk.getPositionY());
            ps.setString(6, desk.getDeskType().name());
            ps.setString(7, desk.getStatus().name());
            ps.setBoolean(8, desk.isHasMonitor());
            ps.setBoolean(9, desk.isHasPhone());
            ps.setBoolean(10, desk.isHasPowerOutlet());
            ps.setString(11, desk.getDescription());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating desk", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return -1;
    }

    @Override
    public boolean update(Desk desk) {
        String sql = "UPDATE desks SET desk_code=?, floor=?, zone=?, position_x=?, position_y=?, desk_type=?, status=?, has_monitor=?, has_phone=?, has_power_outlet=?, description=? WHERE id=?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, desk.getDeskCode());
            ps.setInt(2, desk.getFloor());
            ps.setString(3, desk.getZone());
            ps.setInt(4, desk.getPositionX());
            ps.setInt(5, desk.getPositionY());
            ps.setString(6, desk.getDeskType().name());
            ps.setString(7, desk.getStatus().name());
            ps.setBoolean(8, desk.isHasMonitor());
            ps.setBoolean(9, desk.isHasPhone());
            ps.setBoolean(10, desk.isHasPowerOutlet());
            ps.setString(11, desk.getDescription());
            ps.setInt(12, desk.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating desk", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return false;
    }

    @Override
    public boolean updateStatus(int deskId, String status) {
        String sql = "UPDATE desks SET status = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, deskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating desk status", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return false;
    }

    @Override
    public int countAll() {
        return countQuery("SELECT COUNT(*) FROM desks WHERE status != 'INACTIVE'");
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM desks WHERE status = ?";
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting desks", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return 0;
    }

    @Override
    public List<String> findAllZones() {
        String sql = "SELECT DISTINCT zone FROM desks ORDER BY zone";
        List<String> zones = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) zones.add(rs.getString("zone"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding zones", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return zones;
    }

    private int countQuery(String sql) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error in count query", e);
        } finally {
            dbConfig.releaseConnection(conn);
        }
        return 0;
    }
}
