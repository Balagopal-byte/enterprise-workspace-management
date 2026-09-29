package com.workspace.management.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application-wide configuration loaded from application.properties.
 * Provides centralized access to all configurable settings.
 */
public class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static AppConfig instance;
    private final Properties properties;

    // Default values
    private static final int DEFAULT_GRACE_PERIOD = 15;
    private static final int DEFAULT_CHECK_INTERVAL = 60;
    private static final int DEFAULT_REFRESH_INTERVAL = 30;
    private static final int DEFAULT_MAX_BOOKING_HOURS = 10;
    private static final int DEFAULT_MIN_BOOKING_MINUTES = 30;

    private AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                properties.load(is);
                LOGGER.info("Application properties loaded successfully");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading application.properties", e);
        }
    }

    public String getAppName() {
        return properties.getProperty("app.name", "Enterprise Workspace Management System");
    }

    public String getAppVersion() {
        return properties.getProperty("app.version", "1.0.0");
    }

    public int getCheckinGracePeriodMinutes() {
        return getInt("checkin.grace.period.minutes", DEFAULT_GRACE_PERIOD);
    }

    public boolean isAutoReleaseEnabled() {
        return Boolean.parseBoolean(properties.getProperty("autorelease.enabled", "true"));
    }

    public int getAutoReleaseCheckIntervalSeconds() {
        return getInt("autorelease.check.interval.seconds", DEFAULT_CHECK_INTERVAL);
    }

    public int getRefreshIntervalSeconds() {
        return getInt("refresh.interval.seconds", DEFAULT_REFRESH_INTERVAL);
    }

    public int getMaxBookingDurationHours() {
        return getInt("booking.max.duration.hours", DEFAULT_MAX_BOOKING_HOURS);
    }

    public int getMinBookingDurationMinutes() {
        return getInt("booking.min.duration.minutes", DEFAULT_MIN_BOOKING_MINUTES);
    }

    private int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
