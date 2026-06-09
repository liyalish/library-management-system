package com.library.service;

import com.library.dao.UserDao;
import com.library.exception.ServiceException;
import com.library.model.Role;
import com.library.model.User;
import com.library.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserServiceImpl}. The DAO is mocked so the service's business
 * logic (uniqueness checks, password hashing, authentication, blocking) is tested in
 * isolation, without a real database.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setUsername("alice");
        existingUser.setPasswordHash(PasswordUtil.hash("secret123"));
        existingUser.setFullName("Alice Smith");
        existingUser.setEmail("alice@mail.com");
        existingUser.setRole(Role.READER);
        existingUser.setBlocked(false);
    }

    // ---------- register ----------

    @Test
    void register_withNewUsername_createsUser() {
        when(userDao.findByUsername("bob")).thenReturn(Optional.empty());
        when(userDao.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register("bob", "pass1234", "Bob Jones", "bob@mail.com");

        assertNotNull(result);
        assertEquals("bob", result.getUsername());
        assertEquals(Role.READER, result.getRole());
        // Password must be hashed, never stored as plain text.
        assertEquals(true, PasswordUtil.matches("pass1234", result.getPasswordHash()));
        verify(userDao).create(any(User.class));
    }

    @Test
    void register_withTakenUsername_throwsException() {
        when(userDao.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        assertThrows(ServiceException.class,
                () -> userService.register("alice", "pass1234", "Alice", "a@mail.com"));
        // Must not attempt to create a duplicate.
        verify(userDao, never()).create(any(User.class));
    }

    // ---------- authenticate ----------

    @Test
    void authenticate_withCorrectPassword_returnsUser() {
        when(userDao.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        User result = userService.authenticate("alice", "secret123");

        assertEquals("alice", result.getUsername());
    }

    @Test
    void authenticate_withWrongPassword_throwsException() {
        when(userDao.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        assertThrows(ServiceException.class,
                () -> userService.authenticate("alice", "wrongpass"));
    }

    @Test
    void authenticate_withUnknownUser_throwsException() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> userService.authenticate("ghost", "whatever"));
    }

    @Test
    void authenticate_withBlockedUser_throwsException() {
        existingUser.setBlocked(true);
        when(userDao.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        assertThrows(ServiceException.class,
                () -> userService.authenticate("alice", "secret123"));
    }

    // ---------- setBlocked ----------

    @Test
    void setBlocked_existingUser_updatesUser() {
        when(userDao.findById(1)).thenReturn(Optional.of(existingUser));

        userService.setBlocked(1, true);

        assertEquals(true, existingUser.isBlocked());
        verify(userDao).update(existingUser);
    }

    @Test
    void setBlocked_unknownUser_throwsException() {
        when(userDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> userService.setBlocked(99, true));
        verify(userDao, never()).update(any(User.class));
    }

    // ---------- pagination passthrough ----------

    @Test
    void getUsers_computesOffsetFromPage() {
        userService.getUsers(3, 5);
        // page 3, size 5 -> offset 10
        verify(userDao).findAll(5, 10);
    }
}