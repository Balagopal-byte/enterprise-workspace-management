package com.workspace.management.service;

import com.workspace.management.dao.AnalyticsDAO;
import com.workspace.management.dao.impl.AnalyticsDAOImpl;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service for analytics and reporting.
 * Aggregates data from AnalyticsDAO for dashboard display.
 */
public class AnalyticsService {
    private final AnalyticsDAO analyticsDAO;

    public AnalyticsService() {
        this.analyticsDAO = new AnalyticsDAOImpl();
    }

    // --- Dashboard Summary ---
    public int getTotalDesks() { return analyticsDAO.getTotalDesks(); }
    public int getAvailableDesks() { return analyticsDAO.getAvailableDesks(); }
    public int getOccupiedDesks() { return analyticsDAO.getOccupiedDesks(); }
    public int getTotalRooms() { return analyticsDAO.getTotalRooms(); }
    public int getAvailableRooms() { return analyticsDAO.getAvailableRooms(); }
    public int getTodayBookingCount() { return analyticsDAO.getTodayBookingCount(); }
    public double getCurrentOccupancy() { return analyticsDAO.getCurrentOccupancyPercentage(); }

    // --- Charts ---
    public Map<String, Integer> getDeskUtilizationByZone(LocalDate start, LocalDate end) {
        return analyticsDAO.getDeskUtilizationByZone(start, end);
    }

    public Map<Integer, Integer> getOccupancyByHour(LocalDate date) {
        return analyticsDAO.getOccupancyByHour(date);
    }

    public Map<String, Integer> getBookingsByStatus(LocalDate start, LocalDate end) {
        return analyticsDAO.getBookingsByStatus(start, end);
    }

    public Map<LocalDate, Integer> getBookingTrend(LocalDate start, LocalDate end) {
        return analyticsDAO.getBookingTrend(start, end);
    }

    public Map<String, Integer> getRoomUtilization(LocalDate start, LocalDate end) {
        return analyticsDAO.getRoomUtilization(start, end);
    }

    // --- Detailed Metrics ---
    public int getBookingCount(LocalDate start, LocalDate end) { return analyticsDAO.getBookingCount(start, end); }
    public int getCancellationCount(LocalDate start, LocalDate end) { return analyticsDAO.getCancellationCount(start, end); }
    public int getAutoReleaseCount(LocalDate start, LocalDate end) { return analyticsDAO.getAutoReleaseCount(start, end); }
    public int getCheckInCount(LocalDate start, LocalDate end) { return analyticsDAO.getCheckInCount(start, end); }
    public double getCheckInRate(LocalDate start, LocalDate end) { return analyticsDAO.getCheckInRate(start, end); }
    public double getAverageOccupancy(LocalDate start, LocalDate end) { return analyticsDAO.getAverageOccupancy(start, end); }
    public double getPeakOccupancy(LocalDate start, LocalDate end) { return analyticsDAO.getPeakOccupancy(start, end); }

    // --- Audit ---
    public void logAudit(int userId, String action, String entityType, Integer entityId, String description) {
        analyticsDAO.logAudit(userId, action, entityType, entityId, description);
    }
}
