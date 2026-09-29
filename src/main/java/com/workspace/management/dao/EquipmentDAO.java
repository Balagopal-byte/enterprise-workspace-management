package com.workspace.management.dao;

import com.workspace.management.model.Equipment;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Equipment operations.
 */
public interface EquipmentDAO {
    Optional<Equipment> findById(int id);
    List<Equipment> findAll();
    List<Equipment> findByRoomId(int roomId);
    List<Equipment> findActive();
    int create(Equipment equipment);
    boolean update(Equipment equipment);
    boolean updateStatus(int equipmentId, String status);
    boolean addToRoom(int roomId, int equipmentId, int quantity);
    boolean removeFromRoom(int roomId, int equipmentId);
    List<Equipment> findByRoom(int roomId);
}
