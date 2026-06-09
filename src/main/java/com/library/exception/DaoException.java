package com.library.exception;

/**
 * Thrown when a data-access operation fails. Wraps lower-level {@link java.sql.SQLException}
 * so that the service and controller layers do not depend on JDBC details.
 */
public class DaoException extends RuntimeException {

    /**
     * Creates a new DaoException with a message and underlying cause.
     *
     * @param message description of what failed
     * @param cause   the original exception (e.g. SQLException)
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new DaoException with a message only.
     *
     * @param message description of what failed
     */
    public DaoException(String message) {
        super(message);
    }
}