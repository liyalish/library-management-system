package com.library.service;

import com.library.model.User;

import java.util.List;

/**
 * Business operations for user registration, authentication, and management.
 */
public interface UserService {

    /**
     * Registers a new reader account. The password is hashed before storage and the
     * username must be unique.
     *
     * @param username      desired unique username
     * @param plainPassword raw password (will be hashed with BCrypt)
     * @param fullName      user's full name
     * @param email         user's email address
     * @return the newly created user
     * @throws com.library.exception.ServiceException if the username already exists
     */
    User register(String username, String plainPassword, String fullName, String email);

    /**
     * Authenticates a user by username and password.
     *
     * @param username      the username
     * @param plainPassword the raw password to verify
     * @return the authenticated user
     * @throws com.library.exception.ServiceException if credentials are invalid or the
     *                                                account is blocked
     */
    User authenticate(String username, String plainPassword);

    /**
     * Returns a page of users (admin function).
     *
     * @param page     1-based page number
     * @param pageSize number of users per page
     * @return the users on the requested page
     */
    List<User> getUsers(int page, int pageSize);

    /**
     * Returns the total number of users (for pagination controls).
     *
     * @return total user count
     */
    int getUserCount();

    /**
     * Blocks or unblocks a user account (admin function).
     *
     * @param userId  the id of the user
     * @param blocked the new blocked state
     */
    void setBlocked(int userId, boolean blocked);

    /**
     * Deletes a user account (admin function).
     *
     * @param userId the id of the user to delete
     */
    void deleteUser(int userId);
}