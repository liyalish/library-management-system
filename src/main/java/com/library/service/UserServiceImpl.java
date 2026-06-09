package com.library.service;

import com.library.dao.UserDao;
import com.library.exception.ServiceException;
import com.library.model.Role;
import com.library.model.User;
import com.library.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link UserService}. Handles password hashing, uniqueness
 * checks, and authentication. Depends on {@link UserDao} through its interface so the
 * data layer can be swapped or mocked in tests.
 */
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;

    /**
     * Creates the service with the given DAO (constructor injection).
     *
     * @param userDao the user data-access object
     */
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User register(String username, String plainPassword, String fullName, String email) {
        if (userDao.findByUsername(username).isPresent()) {
            throw new ServiceException("Username already taken: " + username);
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hash(plainPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(Role.READER);
        User created = userDao.create(user);
        LOGGER.info("Registered new reader: {}", username);
        return created;
    }

    @Override
    public User authenticate(String username, String plainPassword) {
        Optional<User> found = userDao.findByUsername(username);
        if (found.isEmpty() || !PasswordUtil.matches(plainPassword, found.get().getPasswordHash())) {
            LOGGER.warn("Failed login attempt for username: {}", username);
            throw new ServiceException("Invalid username or password");
        }
        User user = found.get();
        if (user.isBlocked()) {
            throw new ServiceException("This account has been blocked");
        }
        LOGGER.info("User logged in: {}", username);
        return user;
    }

    @Override
    public List<User> getUsers(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return userDao.findAll(pageSize, offset);
    }

    @Override
    public int getUserCount() {
        return userDao.countAll();
    }

    @Override
    public void setBlocked(int userId, boolean blocked) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ServiceException("User not found: " + userId));
        user.setBlocked(blocked);
        userDao.update(user);
        LOGGER.info("User {} blocked={}", userId, blocked);
    }

    @Override
    public void deleteUser(int userId) {
        userDao.delete(userId);
        LOGGER.info("Deleted user {}", userId);
    }
}