package com.workspace.management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a desk booking/reservation.
 * Contains the complete booking lifecycle state.
 */
public class Booking {
    private int id;
    private int userId;
    private int deskId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient fields for display (populated by JOINs)
    private String userName;
    private String userEmail;
    private String deskCode;
    private String deskZone;

    public enum BookingStatus {
        PENDING, CONFIRMED, CHECKED_IN, COMPLETED, CANCELLED, AUTO_RELEASED;

        public String getDisplayName() {
            return name().replace('_', ' ');
        }
    }

    public Booking() {}

    public Booking(int userId, int deskId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        this.userId = userId;
        this.deskId = deskId;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = BookingStatus.CONFIRMED;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getDeskId() { return deskId; }
    public void setDeskId(int deskId) { this.deskId = deskId; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public void setStatus(String status) {
        try { this.status = BookingStatus.valueOf(status); } catch (Exception e) { this.status = BookingStatus.CONFIRMED; }
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getDeskCode() { return deskCode; }
    public void setDeskCode(String deskCode) { this.deskCode = deskCode; }

    public String getDeskZone() { return deskZone; }
    public void setDeskZone(String deskZone) { this.deskZone = deskZone; }

    public boolean isActive() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.CHECKED_IN;
    }

    public boolean canCheckIn() {
        return status == BookingStatus.CONFIRMED;
    }

    public boolean canCancel() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING;
    }

    /**
     * Check if this booking overlaps with another time interval.
     * Two intervals overlap when: existing.start < requested.end AND existing.end > requested.start
     */
    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        return this.startTime.isBefore(otherEnd) && this.endTime.isAfter(otherStart);
    }

    @Override
    public String toString() {
        return "Booking{id=" + id + ", desk=" + deskCode + ", date=" + bookingDate +
               ", time=" + startTime + "-" + endTime + ", status=" + status + "}";
    }
}
