package com.library.dao;

import com.library.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data-access operations for {@link User} entities.
 */
public interface UserDao {

    /**
     * Persists a new user.
     *
     * @param user the user to insert (its generated id is set on return)
     * @return the user with its generated id populated
     */
    User create(User user);

    /**
     * Finds a user by unique username.
     *
     * @param username the username to look up
     * @return an Optional containing the user, or empty if none found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by id.
     *
     * @param userId the user id
     * @return an Optional containing the user, or empty if none found
     */
    Optional<User> findById(int userId);

    /**
     * Returns a page of users ordered by id.
     *
     * @param limit  maximum number of rows
     * @param offset number of rows to skip
     * @return the list of users for the requested page
     */
    List<User> findAll(int limit, int offset);

    /**
     * Counts all users (used for pagination).
     *
     * @return total number of users
     */
    int countAll();

    /**
     * Updates the blocked flag and role of a user.
     *
     * @param user the user carrying the new values
     */
    void update(User user);

    /**
     * Deletes a user by id.
     *
     * @param userId the id of the user to delete
     */
    void delete(int userId);
}