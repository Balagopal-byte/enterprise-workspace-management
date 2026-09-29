package com.workspace.management.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a meeting room with capacity and equipment information.
 */
public class MeetingRoom {
    private int id;
    private String roomName;
    private int floor;
    private int capacity;
    private String location;
    private RoomStatus status;
    private String description;
    private int positionX;
    private int positionY;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient: equipment list for this room
    private List<Equipment> equipmentList = new ArrayList<>();

    public enum RoomStatus {
        AVAILABLE, BOOKED, MAINTENANCE, INACTIVE;

        public String getDisplayName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    public MeetingRoom() {}

    public MeetingRoom(String roomName, int floor, int capacity, String location) {
        this.roomName = roomName;
        this.floor = floor;
        this.capacity = capacity;
        this.location = location;
        this.status = RoomStatus.AVAILABLE;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public void setStatus(String status) {
        try { this.status = RoomStatus.valueOf(status); } catch (Exception e) { this.status = RoomStatus.AVAILABLE; }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPositionX() { return positionX; }
    public void setPositionX(int positionX) { this.positionX = positionX; }

    public int getPositionY() { return positionY; }
    public void setPositionY(int positionY) { this.positionY = positionY; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Equipment> getEquipmentList() { return equipmentList; }
    public void setEquipmentList(List<Equipment> equipmentList) { this.equipmentList = equipmentList; }

    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }

    public String getEquipmentSummary() {
        if (equipmentList == null || equipmentList.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < equipmentList.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(equipmentList.get(i).getName());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "MeetingRoom{name='" + roomName + "', capacity=" + capacity + ", status=" + status + "}";
    }
}
