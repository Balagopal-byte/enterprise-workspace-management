package com.workspace.management.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized database configuration and connection management.
 * Implements a simple connection pool using a BlockingQueue.
 * Demonstrates the Singleton pattern for database access.
 */
public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static DatabaseConfig instance;

    private String url;
    private String username;
    private String password;
    private int poolSize;
    private BlockingQueue<Connection> connectionPool;
    private boolean initialized = false;

    private DatabaseConfig() {
        loadProperties();
    }

    /**
     * Singleton access to the database configuration.
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                LOGGER.warning("db.properties not found, using defaults");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading db.properties", e);
        }

        String host = props.getProperty("db.host", "localhost");
        String port = props.getProperty("db.port", "3306");
        String dbName = props.getProperty("db.name", "enterprise_workspace");
        this.username = props.getProperty("db.user", "root");
        this.password = props.getProperty("db.password", "");
        this.poolSize = Integer.parseInt(props.getProperty("db.pool.size", "10"));

        this.url = "jdbc:mysql://" + host + ":" + port + "/" + dbName +
                   "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8";
    }

    /**
     * Initialize the connection pool.
     */
    public synchronized void initializePool() throws SQLException {
        if (initialized) return;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }

        connectionPool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            connectionPool.offer(createConnection());
        }
        initialized = true;
        LOGGER.info("Database connection pool initialized with " + poolSize + " connections");
    }

    private Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(true);
        return conn;
    }

    /**
     * Get a connection from the pool.
     */
    public Connection getConnection() throws SQLException {
        if (!initialized) {
            initializePool();
        }
        try {
            Connection conn = connectionPool.poll();
            if (conn == null || conn.isClosed() || !conn.isValid(2)) {
                conn = createConnection();
            }
            return conn;
        } catch (Exception e) {
            return createConnection();
        }
    }

    /**
     * Return a connection to the pool.
     */
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && conn.isValid(1)) {
                    conn.setAutoCommit(true);
                    if (!connectionPool.offer(conn)) {
                        conn.close();
                    }
                } else {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.FINE, "Error releasing connection", e);
            }
        }
    }

    /**
     * Close all connections in the pool.
     */
    public synchronized void closeAll() {
        if (connectionPool != null) {
            Connection conn;
            while ((conn = connectionPool.poll()) != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        initialized = false;
        LOGGER.info("All database connections closed");
    }

    /**
     * Test database connectivity.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean valid = conn.isValid(5);
            releaseConnection(conn);
            return valid;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database connection test failed", e);
            return false;
        }
    }

    public String getUrl() { return url; }
    public boolean isInitialized() { return initialized; }
}
