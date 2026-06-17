package com.library.service;

import com.library.dao.UserDao;
import com.library.exception.ServiceException;
import com.library.model.Role;
import com.library.model.User;
import com.library.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;

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
    public User createLibrarian(String username, String plainPassword, String fullName, String email) {
        if (userDao.findByUsername(username).isPresent()) {
            throw new ServiceException("Username already taken: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hash(plainPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(Role.LIBRARIAN);

        User created = userDao.create(user);
        LOGGER.info("Admin created librarian account: {}", username);

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

        if (user.getRole() == Role.ADMIN) {
            throw new ServiceException("Admin account cannot be blocked");
        }

        user.setBlocked(blocked);
        userDao.update(user);

        LOGGER.info("User {} blocked={}", userId, blocked);
    }

    @Override
    public void deleteUser(int userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ServiceException("User not found: " + userId));

        if (user.getRole() != Role.LIBRARIAN) {
            throw new ServiceException("Admin can delete only librarian accounts");
        }

        userDao.delete(userId);
        LOGGER.info("Deleted librarian {}", userId);
    }

    @Override
    public void deleteOwnAccount(int userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ServiceException("User not found: " + userId));

        if (user.getRole() != Role.READER) {
            throw new ServiceException("Only readers can delete their own account");
        }

        int activeRequests = userDao.countActiveRequests(userId);
        if (activeRequests > 0) {
            throw new ServiceException("You cannot delete your account while you have active book requests");
        }

        userDao.delete(userId);
        LOGGER.info("Reader {} deleted own account", userId);
    }
}