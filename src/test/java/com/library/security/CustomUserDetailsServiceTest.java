package com.library.security;

import com.library.dao.UserDao;
import com.library.model.Role;
import com.library.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserDao userDao;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userDao);
    }

    @Test
    void loadUserByUsername_existingReaderReturnsSpringUserDetails() {
        User reader = user("reader", Role.READER, false);
        when(userDao.findByUsername("reader")).thenReturn(Optional.of(reader));

        UserDetails result = service.loadUserByUsername("reader");

        assertEquals("reader", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> "ROLE_READER".equals(a.getAuthority())));
    }

    @Test
    void loadUserByUsername_existingAdminReturnsAdminRole() {
        User admin = user("admin", Role.ADMIN, false);
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails result = service.loadUserByUsername("admin");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @Test
    void loadUserByUsername_blockedUserReturnsLockedAccount() {
        User reader = user("reader", Role.READER, true);
        when(userDao.findByUsername("reader")).thenReturn(Optional.of(reader));

        UserDetails result = service.loadUserByUsername("reader");

        assertFalse(result.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_missingUserThrowsException() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("ghost"));
    }

    private User user(String username, Role role, boolean blocked) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("encoded-password");
        user.setRole(role);
        user.setBlocked(blocked);
        return user;
    }
}
