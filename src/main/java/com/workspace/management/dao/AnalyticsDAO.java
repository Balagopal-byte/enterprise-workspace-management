package com.workspace.management.dao;

import java.time.LocalDate;
import java.util.Map;

/**
 * Data Access Object interface for Analytics and reporting queries.
 */
public interface AnalyticsDAO {
    int getTotalDesks();
    int getAvailableDesks();
    int getOccupiedDesks();
    int getMaintenanceDesks();
    int getTotalRooms();
    int getAvailableRooms();
    int getTodayBookingCount();
    int getTodayRoomBookingCount();
    double getCurrentOccupancyPercentage();

    Map<String, Integer> getDeskUtilizationByZone(LocalDate start, LocalDate end);
    Map<Integer, Integer> getOccupancyByHour(LocalDate date);
    Map<String, Integer> getBookingsByStatus(LocalDate start, LocalDate end);
    Map<LocalDate, Integer> getBookingTrend(LocalDate start, LocalDate end);
    Map<String, Integer> getRoomUtilization(LocalDate start, LocalDate end);

    int getBookingCount(LocalDate start, LocalDate end);
    int getCancellationCount(LocalDate start, LocalDate end);
    int getAutoReleaseCount(LocalDate start, LocalDate end);
    int getCheckInCount(LocalDate start, LocalDate end);
    double getCheckInRate(LocalDate start, LocalDate end);
    double getAverageOccupancy(LocalDate start, LocalDate end);
    double getPeakOccupancy(LocalDate start, LocalDate end);

    /** Log an audit event */
    void logAudit(int userId, String action, String entityType, Integer entityId, String description);
}
