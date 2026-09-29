package com.workspace.management.dao;

import com.workspace.management.model.MeetingRoom;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Meeting Room operations.
 */
public interface MeetingRoomDAO {
    Optional<MeetingRoom> findById(int id);
    List<MeetingRoom> findAll();
    List<MeetingRoom> findByStatus(String status);
    List<MeetingRoom> findByFloor(int floor);
    int create(MeetingRoom room);
    boolean update(MeetingRoom room);
    boolean updateStatus(int roomId, String status);
    int countAll();
    int countByStatus(String status);
}
