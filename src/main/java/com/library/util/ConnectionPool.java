package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A custom, thread-safe JDBC connection pool implemented manually (no external
 * pooling library). Connections are created once at startup and reused.
 *
 * <p>Thread safety is provided by a {@link BlockingQueue}: {@link #getConnection()}
 * blocks until a connection becomes available, and returning a connection is an
 * atomic queue operation. The pool is a lazily-initialized singleton.</p>
 */
public final class ConnectionPool {

    private static volatile ConnectionPool instance;

    private final BlockingQueue<Connection> availableConnections;
    private final List<Connection> allConnections;

    private ConnectionPool() {
        String url = PropertiesLoader.get("db.url");
        String user = PropertiesLoader.get("db.username");
        String password = PropertiesLoader.get("db.password");
        int poolSize = PropertiesLoader.getInt("db.pool.size", 10);

        try {
            // Ensure the driver is registered (explicit for clarity).
            Class.forName(PropertiesLoader.get("db.driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC driver not found", e);
        }

        availableConnections = new ArrayBlockingQueue<>(poolSize);
        allConnections = new ArrayList<>(poolSize);

        try {
            for (int i = 0; i < poolSize; i++) {
                Connection real = DriverManager.getConnection(url, user, password);
                Connection proxy = PooledConnectionProxy.wrap(real, availableConnections);
                availableConnections.offer(proxy);
                allConnections.add(real);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize connection pool", e);
        }
    }

    /**
     * Returns the singleton pool instance, creating it on first access.
     * Uses double-checked locking for thread-safe lazy initialization.
     *
     * @return the connection pool instance
     */
    public static ConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }

    /**
     * Borrows a connection from the pool, waiting if none is currently available.
     * The returned connection is a proxy whose {@code close()} returns it to the pool.
     *
     * @return a usable JDBC connection
     * @throws SQLException if interrupted while waiting for a connection
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = availableConnections.poll(10, TimeUnit.SECONDS);
            if (connection == null) {
                throw new SQLException("Timed out waiting for a free connection");
            }
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a connection", e);
        }
    }

    /**
     * Closes all physical connections. Intended to be called once on application
     * shutdown.
     */
    public void shutdown() {
        for (Connection real : allConnections) {
            try {
                real.close();
            } catch (SQLException ignored) {
                // Best-effort close on shutdown.
            }
        }
        availableConnections.clear();
        allConnections.clear();
    }
}