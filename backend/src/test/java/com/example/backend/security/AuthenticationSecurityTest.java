package com.example.backend.security;

import com.example.backend.dto.request.AuthenticationRequest;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.response.AuthenticationResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationSecurityTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    private String username;
    private String password;

    @BeforeEach
    void setUp() {
        username = "securityuser";
        password = "SecurePassword123";

        // Create user for testing
        UserCreationRequest request = UserCreationRequest.builder()
                .username(username)
                .password(password)
                .fullname("Security Test User")
                .email("security@sis.hust.edu.vn")
                .build();
        userService.createUser(request);
    }

    @Test
    void authenticate_withValidCredentials_shouldReturnToken() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(username)
                .password(password)
                .build();

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertTrue(response.isAuthenticated());
    }

    @Test
    void authenticate_withInvalidUsername_shouldThrowException() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("nonexistent")
                .password(password)
                .build();

        // Act & Assert
        AppException exception = assertThrows(
                AppException.class,
                () -> authenticationService.authenticate(request)
        );

        assertEquals(ErrorCode.USER_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void authenticate_withInvalidPassword_shouldThrowException() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(username)
                .password("wrongpassword")
                .build();

        // Act & Assert
        var exception = assertThrows(
                com.example.backend.exception.AppException.class,
                () -> authenticationService.authenticate(request)
        );

        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }

    @Test
    void authenticate_withSQLInjection_shouldReject() {
        // Arrange: Attempt SQL injection in username
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("admin'; DROP TABLE users; --")
                .password(password)
                .build();

        // Act & Assert: Should not find user (SQL injection prevented)
        var exception = assertThrows(
                com.example.backend.exception.AppException.class,
                () -> authenticationService.authenticate(request)
        );

        assertEquals(ErrorCode.USER_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void authenticate_withEmptyCredentials_shouldThrowException() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("")
                .password("")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> authenticationService.authenticate(request));
    }
}
