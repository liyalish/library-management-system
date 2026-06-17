package com.library.dao;

import com.library.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User create(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findById(int userId);

    List<User> findAll(int limit, int offset);

    int countAll();

    int countActiveRequests(int userId);

    void update(User user);

    void delete(int userId);
}