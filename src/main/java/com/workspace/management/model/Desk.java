package com.workspace.management.model;

import java.time.LocalDateTime;

/**
 * Represents a desk/workspace in the office floor.
 * Stores position data for the interactive floor map.
 */
public class Desk {
    private int id;
    private String deskCode;
    private int floor;
    private String zone;
    private int positionX;
    private int positionY;
    private DeskType deskType;
    private DeskStatus status;
    private boolean hasMonitor;
    private boolean hasPhone;
    private boolean hasPowerOutlet;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient field for current booking info (not persisted)
    private String currentBookingUser;

    public enum DeskType {
        STANDARD, STANDING, CORNER, EXECUTIVE;

        public String getDisplayName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    public enum DeskStatus {
        AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE, INACTIVE;

        public String getDisplayName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    public Desk() {}

    public Desk(String deskCode, int floor, String zone, int positionX, int positionY) {
        this.deskCode = deskCode;
        this.floor = floor;
        this.zone = zone;
        this.positionX = positionX;
        this.positionY = positionY;
        this.deskType = DeskType.STANDARD;
        this.status = DeskStatus.AVAILABLE;
        this.hasMonitor = true;
        this.hasPowerOutlet = true;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDeskCode() { return deskCode; }
    public void setDeskCode(String deskCode) { this.deskCode = deskCode; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public int getPositionX() { return positionX; }
    public void setPositionX(int positionX) { this.positionX = positionX; }

    public int getPositionY() { return positionY; }
    public void setPositionY(int positionY) { this.positionY = positionY; }

    public DeskType getDeskType() { return deskType; }
    public void setDeskType(DeskType deskType) { this.deskType = deskType; }
    public void setDeskType(String deskType) {
        try { this.deskType = DeskType.valueOf(deskType); } catch (Exception e) { this.deskType = DeskType.STANDARD; }
    }

    public DeskStatus getStatus() { return status; }
    public void setStatus(DeskStatus status) { this.status = status; }
    public void setStatus(String status) {
        try { this.status = DeskStatus.valueOf(status); } catch (Exception e) { this.status = DeskStatus.AVAILABLE; }
    }

    public boolean isHasMonitor() { return hasMonitor; }
    public void setHasMonitor(boolean hasMonitor) { this.hasMonitor = hasMonitor; }

    public boolean isHasPhone() { return hasPhone; }
    public void setHasPhone(boolean hasPhone) { this.hasPhone = hasPhone; }

    public boolean isHasPowerOutlet() { return hasPowerOutlet; }
    public void setHasPowerOutlet(boolean hasPowerOutlet) { this.hasPowerOutlet = hasPowerOutlet; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCurrentBookingUser() { return currentBookingUser; }
    public void setCurrentBookingUser(String currentBookingUser) { this.currentBookingUser = currentBookingUser; }

    public boolean isAvailable() { return status == DeskStatus.AVAILABLE; }

    @Override
    public String toString() {
        return "Desk{code='" + deskCode + "', zone='" + zone + "', status=" + status + "}";
    }
}
