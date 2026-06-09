package com.library.dao;

/**
 * Central factory for creating DAO instances (Factory Method design pattern).
 * Keeps DAO construction in one place: callers ask the factory for an interface and
 * receive the configured implementation, so the concrete classes can be swapped without
 * touching client code. Implemented as a thread-safe singleton.
 */
public final class DaoFactory {

    private static volatile DaoFactory instance;

    private DaoFactory() {
        // Singleton — no external instances.
    }

    /**
     * Returns the singleton factory instance (double-checked locking).
     *
     * @return the DAO factory
     */
    public static DaoFactory getInstance() {
        if (instance == null) {
            synchronized (DaoFactory.class) {
                if (instance == null) {
                    instance = new DaoFactory();
                }
            }
        }
        return instance;
    }

    /**
     * Creates a {@link UserDao}.
     *
     * @return a user DAO implementation
     */
    public UserDao createUserDao() {
        return new UserDaoImpl();
    }

    /**
     * Creates a {@link BookDao}.
     *
     * @return a book DAO implementation
     */
    public BookDao createBookDao() {
        return new BookDaoImpl();
    }

    /**
     * Creates a {@link BookRequestDao}.
     *
     * @return a book-request DAO implementation
     */
    public BookRequestDao createBookRequestDao() {
        return new BookRequestDaoImpl();
    }
}