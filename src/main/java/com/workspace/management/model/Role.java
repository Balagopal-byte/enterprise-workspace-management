package com.workspace.management.model;

/**
 * Enumeration representing user roles in the system.
 * Each role has different permission levels.
 */
public enum Role {
    SYSTEM_ADMIN(1, "System Administrator"),
    FACILITY_MANAGER(2, "Facility Manager"),
    EMPLOYEE(3, "Employee");

    private final int id;
    private final String displayName;

    Role(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }

    /**
     * Get Role enum from database role_id.
     */
    public static Role fromId(int id) {
        for (Role role : values()) {
            if (role.id == id) return role;
        }
        throw new IllegalArgumentException("Unknown role id: " + id);
    }

    /**
     * Get Role enum from role_name string.
     */
    public static Role fromName(String name) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(name)) return role;
        }
        throw new IllegalArgumentException("Unknown role name: " + name);
    }

    @Override
    public String toString() { return displayName; }
}
