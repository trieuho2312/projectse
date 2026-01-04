package com.example.backend.util;

import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {

    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        // Lưu context gốc để restore sau test
        originalContext = SecurityContextHolder.getContext();
        // Clear context trước mỗi test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Restore context gốc
        SecurityContextHolder.setContext(originalContext);
    }

    @Test
    void getAuthentication_success() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act
        Authentication result = SecurityUtil.getAuthentication();

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getName());
    }

    @Test
    void getAuthentication_nullAuth_shouldThrowException() {
        // Arrange - context không có authentication
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            SecurityUtil.getAuthentication();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void getAuthentication_notAuthenticated_shouldThrowException() {
        // Arrange - authentication nhưng chưa authenticated
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of()) {
            @Override
            public boolean isAuthenticated() {
                return false;
            }
        };
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            SecurityUtil.getAuthentication();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void getAuthentication_anonymousAuth_shouldThrowException() {
        // Arrange - anonymous authentication
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        Authentication auth = new AnonymousAuthenticationToken("key", "anonymousUser", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            SecurityUtil.getAuthentication();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void getCurrentUsername_success() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act
        String username = SecurityUtil.getCurrentUsername();

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void getCurrentUsername_noAuth_shouldThrowException() {
        // Arrange - không có authentication
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            SecurityUtil.getCurrentUsername();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void hasRole_true() {
        // Arrange - user có role ADMIN
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act
        boolean hasRole = SecurityUtil.hasRole("ADMIN");

        // Assert
        assertTrue(hasRole);
    }

    @Test
    void hasRole_false() {
        // Arrange - user không có role ADMIN
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act
        boolean hasRole = SecurityUtil.hasRole("ADMIN");

        // Assert
        assertFalse(hasRole);
    }

    @Test
    void hasRole_multipleRoles() {
        // Arrange - user có nhiều roles
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act
        boolean hasAdminRole = SecurityUtil.hasRole("ADMIN");
        boolean hasUserRole = SecurityUtil.hasRole("USER");
        boolean hasManagerRole = SecurityUtil.hasRole("MANAGER");

        // Assert
        assertTrue(hasAdminRole);
        assertTrue(hasUserRole);
        assertFalse(hasManagerRole);
    }

    @Test
    void hasRole_caseSensitive() {
        // Arrange - role phải match chính xác
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act
        boolean hasRole = SecurityUtil.hasRole("admin"); // lowercase

        // Assert - không match vì phải là "ADMIN"
        assertFalse(hasRole);
    }

    @Test
    void requireAdmin_success() {
        // Arrange - user có role ADMIN
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act & Assert - không throw exception
        assertDoesNotThrow(() -> {
            SecurityUtil.requireAdmin();
        });
    }

    @Test
    void requireAdmin_noAdminRole_shouldThrowException() {
        // Arrange - user không có role ADMIN
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            SecurityUtil.requireAdmin();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void requireAdmin_noAuth_shouldThrowException() {
        // Arrange - không có authentication
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);

        // Act & Assert - sẽ throw exception ở getAuthentication() trước
        AppException exception = assertThrows(AppException.class, () -> {
            SecurityUtil.requireAdmin();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }
}
