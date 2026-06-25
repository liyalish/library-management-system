package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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
            // Ensure the driver is registered
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