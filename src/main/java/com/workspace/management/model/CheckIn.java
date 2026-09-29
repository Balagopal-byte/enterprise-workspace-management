package com.workspace.management.model;

import java.time.LocalDateTime;

/**
 * Represents a check-in record for a desk booking.
 */
public class CheckIn {
    private int id;
    private int bookingId;
    private int userId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    // Transient
    private String deskCode;
    private String userName;

    public CheckIn() {}

    public CheckIn(int bookingId, int userId) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.checkInTime = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getDeskCode() { return deskCode; }
    public void setDeskCode(String deskCode) { this.deskCode = deskCode; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public boolean isCheckedOut() { return checkOutTime != null; }

    @Override
    public String toString() {
        return "CheckIn{bookingId=" + bookingId + ", checkIn=" + checkInTime + "}";
    }
}
