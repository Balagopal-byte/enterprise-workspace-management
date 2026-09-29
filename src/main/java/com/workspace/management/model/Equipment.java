package com.workspace.management.model;

import java.time.LocalDateTime;

/**
 * Represents equipment that can be assigned to meeting rooms.
 */
public class Equipment {
    private int id;
    private String name;
    private String category;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For room_equipment junction
    private int quantity;

    public Equipment() {}

    public Equipment(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.status = "ACTIVE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isActive() { return "ACTIVE".equals(status); }

    @Override
    public String toString() {
        return name;
    }
}
