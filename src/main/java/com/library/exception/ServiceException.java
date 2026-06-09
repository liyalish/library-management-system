package com.library.exception;

/**
 * Thrown when a business rule is violated in the service layer
 * (for example, registering a username that already exists).
 */
public class ServiceException extends RuntimeException {

    /**
     * Creates a new ServiceException with a message.
     *
     * @param message description of the business error
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Creates a new ServiceException with a message and cause.
     *
     * @param message description of the business error
     * @param cause   the underlying exception
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}