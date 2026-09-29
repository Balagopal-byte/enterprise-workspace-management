package com.workspace.management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an occupancy snapshot for analytics.
 */
public class OccupancyRecord {
    private int id;
    private LocalDate recordDate;
    private int recordHour;
    private int totalDesks;
    private int occupiedDesks;
    private int totalRooms;
    private int occupiedRooms;
    private double occupancyPercentage;
    private LocalDateTime createdAt;

    public OccupancyRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public int getRecordHour() { return recordHour; }
    public void setRecordHour(int recordHour) { this.recordHour = recordHour; }

    public int getTotalDesks() { return totalDesks; }
    public void setTotalDesks(int totalDesks) { this.totalDesks = totalDesks; }

    public int getOccupiedDesks() { return occupiedDesks; }
    public void setOccupiedDesks(int occupiedDesks) { this.occupiedDesks = occupiedDesks; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getOccupiedRooms() { return occupiedRooms; }
    public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }

    public double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
