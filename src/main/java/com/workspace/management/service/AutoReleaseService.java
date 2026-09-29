package com.workspace.management.service;

import com.workspace.management.config.AppConfig;
import com.workspace.management.dao.BookingDAO;
import com.workspace.management.dao.AnalyticsDAO;
import com.workspace.management.dao.impl.BookingDAOImpl;
import com.workspace.management.dao.impl.AnalyticsDAOImpl;
import com.workspace.management.model.Booking;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background service for automatically releasing unchecked-in bookings.
 * Runs on a ScheduledExecutorService to avoid freezing the Swing UI.
 *
 * Module 5: Auto-Release System
 *
 * If a user reserves a desk but does not check in within the grace period,
 * the reservation status is changed to AUTO_RELEASED and the desk becomes available.
 */
public class AutoReleaseService {
    private static final Logger LOGGER = Logger.getLogger(AutoReleaseService.class.getName());
    private final BookingDAO bookingDAO;
    private final AnalyticsDAO analyticsDAO;
    private final AppConfig appConfig;
    private ScheduledExecutorService scheduler;
    private Runnable uiRefreshCallback;
    private volatile boolean running = false;

    public AutoReleaseService() {
        this.bookingDAO = new BookingDAOImpl();
        this.analyticsDAO = new AnalyticsDAOImpl();
        this.appConfig = AppConfig.getInstance();
    }

    /**
     * Start the auto-release background task.
     * Uses ScheduledExecutorService for safe background execution.
     * UI updates are dispatched via SwingUtilities.invokeLater().
     */
    public void start(Runnable uiRefreshCallback) {
        if (running) return;
        if (!appConfig.isAutoReleaseEnabled()) {
            LOGGER.info("Auto-release is disabled in configuration");
            return;
        }

        this.uiRefreshCallback = uiRefreshCallback;
        this.running = true;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoReleaseService");
            t.setDaemon(true);
            return t;
        });

        int intervalSeconds = appConfig.getAutoReleaseCheckIntervalSeconds();

        scheduler.scheduleAtFixedRate(this::checkAndRelease,
                intervalSeconds, // initial delay
                intervalSeconds, // period
                TimeUnit.SECONDS);

        LOGGER.info("Auto-release service started (interval: " + intervalSeconds + "s, grace: " +
                appConfig.getCheckinGracePeriodMinutes() + "min)");
    }

    /**
     * Stop the auto-release service.
     */
    public void stop() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Auto-release service stopped");
    }

    /**
     * Core auto-release logic.
     * Finds bookings past the grace period that haven't been checked in,
     * and changes their status to AUTO_RELEASED.
     */
    private void checkAndRelease() {
        try {
            int gracePeriod = appConfig.getCheckinGracePeriodMinutes();
            List<Booking> expiredBookings = bookingDAO.findBookingsForAutoRelease(gracePeriod);

            if (!expiredBookings.isEmpty()) {
                LOGGER.info("Auto-releasing " + expiredBookings.size() + " unchecked bookings");
            }

            for (Booking booking : expiredBookings) {
                boolean released = bookingDAO.updateStatus(booking.getId(), "AUTO_RELEASED");
                if (released) {
                    analyticsDAO.logAudit(0, "AUTO_RELEASE", "BOOKING", booking.getId(),
                            "Booking auto-released: desk " + booking.getDeskCode() +
                            " for user " + booking.getUserName() +
                            " (not checked in within " + gracePeriod + " minutes)");

                    LOGGER.info("Auto-released booking " + booking.getId() +
                            " (desk " + booking.getDeskCode() + ")");
                }
            }

            // Refresh UI on EDT if any bookings were released
            if (!expiredBookings.isEmpty() && uiRefreshCallback != null) {
                SwingUtilities.invokeLater(uiRefreshCallback);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in auto-release check", e);
        }
    }

    public boolean isRunning() { return running; }
}
