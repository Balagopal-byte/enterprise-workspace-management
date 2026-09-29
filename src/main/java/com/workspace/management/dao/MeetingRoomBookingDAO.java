package com.workspace.management.dao;

import com.workspace.management.model.MeetingRoomBooking;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Meeting Room Booking operations.
 */
public interface MeetingRoomBookingDAO {
    Optional<MeetingRoomBooking> findById(int id);
    List<MeetingRoomBooking> findByUserId(int userId);
    List<MeetingRoomBooking> findByRoomId(int roomId);
    List<MeetingRoomBooking> findByDate(LocalDate date);
    List<MeetingRoomBooking> findAll();
    List<MeetingRoomBooking> findByRoomAndDate(int roomId, LocalDate date);
    List<MeetingRoomBooking> findUpcomingByUserId(int userId);

    boolean hasOverlappingBooking(Connection conn, int roomId, LocalDate date,
                                   LocalTime startTime, LocalTime endTime, int excludeBookingId);

    int create(Connection conn, MeetingRoomBooking booking);
    int create(MeetingRoomBooking booking);
    boolean updateStatus(int bookingId, String status);
    int countByDate(LocalDate date);
    int countByStatus(String status);
}
