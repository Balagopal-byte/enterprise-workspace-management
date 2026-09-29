package com.workspace.management.dao;

import com.workspace.management.model.Desk;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Desk operations.
 */
public interface DeskDAO {
    Optional<Desk> findById(int id);
    Optional<Desk> findByCode(String code);
    List<Desk> findAll();
    List<Desk> findByFloor(int floor);
    List<Desk> findByZone(String zone);
    List<Desk> findByStatus(String status);
    int create(Desk desk);
    boolean update(Desk desk);
    boolean updateStatus(int deskId, String status);
    int countAll();
    int countByStatus(String status);
    List<String> findAllZones();
}
