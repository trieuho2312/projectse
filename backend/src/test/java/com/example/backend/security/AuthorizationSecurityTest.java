package com.example.backend.security;

import com.example.backend.exception.AppException;
import com.example.backend.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthorizationSecurityTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsername_withAuthenticatedUser_shouldReturnUsername() {
        // Arrange: Set up authenticated user
        String username = "testuser";
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        String currentUsername = SecurityUtil.getCurrentUsername();

        // Assert
        assertEquals(username, currentUsername);
    }

    @Test
    void getCurrentUsername_withoutAuthentication_shouldThrowException() {
        // Arrange: No authentication
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThrows(
                AppException.class,
                () -> SecurityUtil.getCurrentUsername()
        );
    }

    @Test
    void hasRole_withAdminRole_shouldReturnTrue() {
        // Arrange: Set up admin user
        String username = "admin";
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        boolean hasAdmin = SecurityUtil.hasRole("ADMIN");

        // Assert
        assertTrue(hasAdmin);
    }

    @Test
    void hasRole_withUserRole_shouldReturnFalse() {
        // Arrange: Set up regular user
        String username = "user";
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        boolean hasAdmin = SecurityUtil.hasRole("ADMIN");

        // Assert
        assertFalse(hasAdmin);
    }

    @Test
    void requireAdmin_withAdminRole_shouldNotThrow() {
        // Arrange: Set up admin user
        String username = "admin";
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act & Assert: Should not throw
        assertDoesNotThrow(() -> SecurityUtil.requireAdmin());
    }

    @Test
    void requireAdmin_withUserRole_shouldThrowException() {
        // Arrange: Set up regular user
        String username = "user";
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act & Assert: Should throw unauthorized exception
        assertThrows(
                AppException.class,
                () -> SecurityUtil.requireAdmin()
        );
    }
}
