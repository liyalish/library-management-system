package com.library.service;

import com.library.dao.UserDao;
import com.library.exception.ServiceException;
import com.library.model.Role;
import com.library.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User reader;
    private User librarian;
    private User admin;

    @BeforeEach
    void setUp() {
        reader = user(1, "reader", Role.READER);
        librarian = user(2, "librarian", Role.LIBRARIAN);
        admin = user(3, "admin", Role.ADMIN);
    }

    @Test
    void register_withNewUsernameCreatesReaderWithEncodedPassword() {
        when(userDao.findByUsername("bob")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded-pass1234");
        when(userDao.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register("bob", "pass1234", "Bob Jones", "bob@mail.com");

        assertEquals("bob", result.getUsername());
        assertEquals("Bob Jones", result.getFullName());
        assertEquals("bob@mail.com", result.getEmail());
        assertEquals(Role.READER, result.getRole());
        assertEquals("encoded-pass1234", result.getPasswordHash());
        assertNotEquals("pass1234", result.getPasswordHash());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).create(captor.capture());
        assertEquals(Role.READER, captor.getValue().getRole());
        assertEquals("encoded-pass1234", captor.getValue().getPasswordHash());
    }

    @Test
    void register_withTakenUsernameThrowsException() {
        when(userDao.findByUsername("reader")).thenReturn(Optional.of(reader));

        assertThrows(ServiceException.class,
                () -> userService.register("reader", "pass1234", "Reader", "r@mail.com"));

        verify(userDao).findByUsername("reader");
        verify(userDao, never()).create(any(User.class));
    }

    @Test
    void createLibrarian_withNewUsernameCreatesLibrarianWithEncodedPassword() {
        when(userDao.findByUsername("lib2")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded-lib-pass");
        when(userDao.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createLibrarian("lib2", "pass1234", "Second Librarian", "lib2@mail.com");

        assertEquals("lib2", result.getUsername());
        assertEquals(Role.LIBRARIAN, result.getRole());
        assertEquals("encoded-lib-pass", result.getPasswordHash());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).create(captor.capture());
        assertEquals(Role.LIBRARIAN, captor.getValue().getRole());
        assertEquals("encoded-lib-pass", captor.getValue().getPasswordHash());
    }

    @Test
    void createLibrarian_withTakenUsernameThrowsException() {
        when(userDao.findByUsername("librarian")).thenReturn(Optional.of(librarian));

        assertThrows(ServiceException.class,
                () -> userService.createLibrarian("librarian", "pass1234", "Lib", "lib@mail.com"));

        verify(userDao).findByUsername("librarian");
        verify(userDao, never()).create(any(User.class));
    }

    @Test
    void getByUsername_existingUserReturnsUser() {
        when(userDao.findByUsername("reader")).thenReturn(Optional.of(reader));

        User result = userService.getByUsername("reader");

        assertSame(reader, result);
        verify(userDao).findByUsername("reader");
        verifyNoMoreInteractions(userDao);
    }

    @Test
    void getByUsername_missingUserThrowsException() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> userService.getByUsername("ghost"));

        verify(userDao).findByUsername("ghost");
    }

    @Test
    void getUsers_pageOneUsesZeroOffset() {
        List<User> users = List.of(reader, librarian);
        when(userDao.findAll(5, 0)).thenReturn(users);

        List<User> result = userService.getUsers(1, 5);

        assertSame(users, result);
        verify(userDao).findAll(5, 0);
    }

    @Test
    void getUsers_pageThreeComputesOffset() {
        userService.getUsers(3, 5);

        verify(userDao).findAll(5, 10);
    }

    @Test
    void getUserCountReturnsDaoCount() {
        when(userDao.countAll()).thenReturn(3);

        int result = userService.getUserCount();

        assertEquals(3, result);
        verify(userDao).countAll();
    }

    @Test
    void setBlocked_existingReaderUpdatesUser() {
        when(userDao.findById(1)).thenReturn(Optional.of(reader));

        userService.setBlocked(1, true);

        assertEquals(true, reader.isBlocked());
        verify(userDao).update(reader);
    }

    @Test
    void setBlocked_existingLibrarianUpdatesUser() {
        when(userDao.findById(2)).thenReturn(Optional.of(librarian));

        userService.setBlocked(2, true);

        assertEquals(true, librarian.isBlocked());
        verify(userDao).update(librarian);
    }

    @Test
    void setBlocked_adminThrowsException() {
        when(userDao.findById(3)).thenReturn(Optional.of(admin));

        assertThrows(ServiceException.class,
                () -> userService.setBlocked(3, true));

        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void setBlocked_unknownUserThrowsException() {
        when(userDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> userService.setBlocked(99, true));

        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void deleteUser_librarianDeletesUser() {
        when(userDao.findById(2)).thenReturn(Optional.of(librarian));

        userService.deleteUser(2);

        verify(userDao).delete(2);
    }

    @Test
    void deleteUser_readerThrowsException() {
        when(userDao.findById(1)).thenReturn(Optional.of(reader));

        assertThrows(ServiceException.class,
                () -> userService.deleteUser(1));

        verify(userDao, never()).delete(1);
    }

    @Test
    void deleteUser_adminThrowsException() {
        when(userDao.findById(3)).thenReturn(Optional.of(admin));

        assertThrows(ServiceException.class,
                () -> userService.deleteUser(3));

        verify(userDao, never()).delete(3);
    }

    @Test
    void deleteUser_unknownUserThrowsException() {
        when(userDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> userService.deleteUser(99));

        verify(userDao, never()).delete(99);
    }

    @Test
    void deleteOwnAccount_readerWithoutActiveRequestsDeletesAccount() {
        when(userDao.findById(1)).thenReturn(Optional.of(reader));
        when(userDao.countActiveRequests(1)).thenReturn(0);

        userService.deleteOwnAccount(1);

        verify(userDao).delete(1);
    }

    @Test
    void deleteOwnAccount_readerWithActiveRequestsThrowsException() {
        when(userDao.findById(1)).thenReturn(Optional.of(reader));
        when(userDao.countActiveRequests(1)).thenReturn(2);

        assertThrows(ServiceException.class,
                () -> userService.deleteOwnAccount(1));

        verify(userDao, never()).delete(1);
    }

    @Test
    void deleteOwnAccount_librarianThrowsException() {
        when(userDao.findById(2)).thenReturn(Optional.of(librarian));

        assertThrows(ServiceException.class,
                () -> userService.deleteOwnAccount(2));

        verify(userDao, never()).delete(2);
        verify(userDao, never()).countActiveRequests(2);
    }

    @Test
    void deleteOwnAccount_adminThrowsException() {
        when(userDao.findById(3)).thenReturn(Optional.of(admin));

        assertThrows(ServiceException.class,
                () -> userService.deleteOwnAccount(3));

        verify(userDao, never()).delete(3);
        verify(userDao, never()).countActiveRequests(3);
    }

    @Test
    void deleteOwnAccount_unknownUserThrowsException() {
        when(userDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> userService.deleteOwnAccount(99));

        verify(userDao, never()).delete(99);
    }

    private User user(int id, String username, Role role) {
        User user = new User();
        user.setUserId(id);
        user.setUsername(username);
        user.setPasswordHash("encoded-" + username);
        user.setFullName(username + " full name");
        user.setEmail(username + "@mail.com");
        user.setRole(role);
        user.setBlocked(false);
        return user;
    }
}
