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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User reader;
    private User librarian;
    private User admin;

    @BeforeEach
    void setUp() {
        reader = new User();
        reader.setUserId(1);
        reader.setUsername("reader");
        reader.setPasswordHash(PasswordUtil.hash("secret123"));
        reader.setFullName("Test Reader");
        reader.setEmail("reader@mail.com");
        reader.setRole(Role.READER);
        reader.setBlocked(false);

        librarian = new User();
        librarian.setUserId(2);
        librarian.setUsername("librarian");
        librarian.setPasswordHash(PasswordUtil.hash("secret123"));
        librarian.setFullName("Main Librarian");
        librarian.setEmail("librarian@mail.com");
        librarian.setRole(Role.LIBRARIAN);
        librarian.setBlocked(false);

        admin = new User();
        admin.setUserId(3);
        admin.setUsername("admin");
        admin.setPasswordHash(PasswordUtil.hash("secret123"));
        admin.setFullName("System Admin");
        admin.setEmail("admin@mail.com");
        admin.setRole(Role.ADMIN);
        admin.setBlocked(false);
    }

    @Test
    void register_withNewUsername_createsReader() {
        when(userDao.findByUsername("bob")).thenReturn(Optional.empty());
        when(userDao.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register("bob", "pass1234", "Bob Jones", "bob@mail.com");

        assertNotNull(result);
        assertEquals("bob", result.getUsername());
        assertEquals(Role.READER, result.getRole());
        assertTrue(PasswordUtil.matches("pass1234", result.getPasswordHash()));

        verify(userDao).create(any(User.class));
    }

    @Test
    void register_withTakenUsername_throwsException() {
        when(userDao.findByUsername("reader")).thenReturn(Optional.of(reader));

        assertThrows(ServiceException.class,
                () -> userService.register("reader", "pass1234", "Reader", "r@mail.com"));

        verify(userDao, never()).create(any(User.class));
    }

    @Test
    void createLibrarian_withNewUsername_createsLibrarian() {
        when(userDao.findByUsername("lib2")).thenReturn(Optional.empty());
        when(userDao.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createLibrarian("lib2", "pass1234", "Second Librarian", "lib2@mail.com");

        assertNotNull(result);
        assertEquals("lib2", result.getUsername());
        assertEquals(Role.LIBRARIAN, result.getRole());
        assertTrue(PasswordUtil.matches("pass1234", result.getPasswordHash()));

        verify(userDao).create(any(User.class));
    }

    @Test
    void createLibrarian_withTakenUsername_throwsException() {
        when(userDao.findByUsername("librarian")).thenReturn(Optional.of(librarian));

        assertThrows(ServiceException.class,
                () -> userService.createLibrarian("librarian", "pass1234", "Lib", "lib@mail.com"));

        verify(userDao, never()).create(any(User.class));
    }

    @Test
    void authenticate_withCorrectPassword_returnsUser() {
        when(userDao.findByUsername("reader")).thenReturn(Optional.of(reader));

        User result = userService.authenticate("reader", "secret123");

        assertEquals("reader", result.getUsername());
    }

    @Test
    void authenticate_withWrongPassword_throwsException() {
        when(userDao.findByUsername("reader")).thenReturn(Optional.of(reader));

        assertThrows(ServiceException.class,
                () -> userService.authenticate("reader", "wrongpass"));
    }

    @Test
    void authenticate_withUnknownUser_throwsException() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> userService.authenticate("ghost", "whatever"));
    }

    @Test
    void authenticate_withBlockedUser_throwsException() {
        reader.setBlocked(true);
        when(userDao.findByUsername("reader")).thenReturn(Optional.of(reader));

        assertThrows(ServiceException.class,
                () -> userService.authenticate("reader", "secret123"));
    }

    @Test
    void setBlocked_existingReader_updatesUser() {
        when(userDao.findById(1)).thenReturn(Optional.of(reader));

        userService.setBlocked(1, true);

        assertEquals(true, reader.isBlocked());
        verify(userDao).update(reader);
    }

    @Test
    void setBlocked_admin_throwsException() {
        when(userDao.findById(3)).thenReturn(Optional.of(admin));

        assertThrows(ServiceException.class,
                () -> userService.setBlocked(3, true));

        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void setBlocked_unknownUser_throwsException() {
        when(userDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> userService.setBlocked(99, true));

        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void deleteUser_librarian_deletesUser() {
        when(userDao.findById(2)).thenReturn(Optional.of(librarian));

        userService.deleteUser(2);

        verify(userDao).delete(2);
    }

    @Test
    void deleteUser_reader_throwsException() {
        when(userDao.findById(1)).thenReturn(Optional.of(reader));

        assertThrows(ServiceException.class,
                () -> userService.deleteUser(1));

        verify(userDao, never()).delete(1);
    }

    @Test
    void deleteUser_admin_throwsException() {
        when(userDao.findById(3)).thenReturn(Optional.of(admin));

        assertThrows(ServiceException.class,
                () -> userService.deleteUser(3));

        verify(userDao, never()).delete(3);
    }

    @Test
    void deleteOwnAccount_readerWithoutActiveRequests_deletesAccount() {
        when(userDao.findById(1)).thenReturn(Optional.of(reader));
        when(userDao.countActiveRequests(1)).thenReturn(0);

        userService.deleteOwnAccount(1);

        verify(userDao).delete(1);
    }

    @Test
    void deleteOwnAccount_readerWithActiveRequests_throwsException() {
        when(userDao.findById(1)).thenReturn(Optional.of(reader));
        when(userDao.countActiveRequests(1)).thenReturn(2);

        assertThrows(ServiceException.class,
                () -> userService.deleteOwnAccount(1));

        verify(userDao, never()).delete(1);
    }

    @Test
    void deleteOwnAccount_librarian_throwsException() {
        when(userDao.findById(2)).thenReturn(Optional.of(librarian));

        assertThrows(ServiceException.class,
                () -> userService.deleteOwnAccount(2));

        verify(userDao, never()).delete(2);
    }

    @Test
    void getUsers_computesOffsetFromPage() {
        userService.getUsers(3, 5);

        verify(userDao).findAll(5, 10);
    }
}