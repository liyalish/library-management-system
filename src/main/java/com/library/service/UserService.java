package com.library.service;

import com.library.model.User;

import java.util.List;

public interface UserService {
    User register(String username, String plainPassword, String fullName, String email);

    User createLibrarian(String username, String plainPassword, String fullName, String email);

    User getByUsername(String username);

    List<User> getUsers(int page, int pageSize);

    int getUserCount();

    void setBlocked(int userId, boolean blocked);

    void deleteUser(int userId);

    void deleteOwnAccount(int userId);
}