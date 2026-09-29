package com.workspace.management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a meeting room booking/reservation.
 */
public class MeetingRoomBooking {
    private int id;
    private int userId;
    private int roomId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String title;
    private String description;
    private int attendeeCount;
    private MeetingBookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient display fields
    private String userName;
    private String roomName;

    public enum MeetingBookingStatus {
        CONFIRMED, COMPLETED, CANCELLED;

        public String getDisplayName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    public MeetingRoomBooking() {}

    public MeetingRoomBooking(int userId, int roomId, LocalDate bookingDate,
                               LocalTime startTime, LocalTime endTime, String title) {
        this.userId = userId;
        this.roomId = roomId;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.status = MeetingBookingStatus.CONFIRMED;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAttendeeCount() { return attendeeCount; }
    public void setAttendeeCount(int attendeeCount) { this.attendeeCount = attendeeCount; }

    public MeetingBookingStatus getStatus() { return status; }
    public void setStatus(MeetingBookingStatus status) { this.status = status; }
    public void setStatus(String status) {
        try { this.status = MeetingBookingStatus.valueOf(status); } catch (Exception e) { this.status = MeetingBookingStatus.CONFIRMED; }
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public boolean canCancel() {
        return status == MeetingBookingStatus.CONFIRMED;
    }

    /**
     * Check if this booking overlaps with another time interval.
     */
    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        return this.startTime.isBefore(otherEnd) && this.endTime.isAfter(otherStart);
    }

    @Override
    public String toString() {
        return "MeetingRoomBooking{id=" + id + ", room=" + roomName +
               ", date=" + bookingDate + ", time=" + startTime + "-" + endTime + "}";
    }
}
