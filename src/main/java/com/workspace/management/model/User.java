package com.workspace.management.model;

import java.time.LocalDateTime;

/**
 * Represents a user in the workspace management system.
 * Demonstrates encapsulation with private fields and public accessors.
 */
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private Role role;
    private int roleId;
    private String department;
    private String phone;
    private String status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Default constructor */
    public User() {}

    /** Constructor for creating new users */
    public User(String firstName, String lastName, String email, String passwordHash, int roleId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.status = "ACTIVE";
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) {
        this.roleId = roleId;
        try { this.role = Role.fromId(roleId); } catch (Exception ignored) {}
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return "ACTIVE".equals(status); }

    public boolean isAdmin() { return role == Role.SYSTEM_ADMIN; }
    public boolean isManager() { return role == Role.FACILITY_MANAGER; }
    public boolean isEmployee() { return role == Role.EMPLOYEE; }

    /**
     * Check if user has at least the given role level.
     * SYSTEM_ADMIN > FACILITY_MANAGER > EMPLOYEE
     */
    public boolean hasRole(Role requiredRole) {
        if (role == null) return false;
        return role.getId() <= requiredRole.getId();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + getFullName() + "', email='" + email + "', role=" + role + "}";
    }
}
