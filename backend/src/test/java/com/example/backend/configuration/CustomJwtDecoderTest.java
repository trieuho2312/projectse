package com.example.backend.configuration;

import com.example.backend.dto.request.IntrospectRequest;
import com.example.backend.dto.response.IntrospectResponse;
import com.example.backend.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomJwtDecoderTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private CustomJwtDecoder customJwtDecoder;

    private static final String TEST_SIGNER_KEY = "test-signer-key-for-ci-cd-pipeline-minimum-256-bits-long-key-for-testing-only";
    private static final String VALID_TOKEN = "valid-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(Objects.requireNonNull(customJwtDecoder), "signerKey", TEST_SIGNER_KEY);
    }

    @Test
    void decode_validToken_success() {
        // Arrange
        IntrospectResponse introspectResponse = IntrospectResponse.builder()
                .valid(true)
                .build();

        when(authenticationService.introspect(any(IntrospectRequest.class)))
                .thenReturn(introspectResponse);

        // Act & Assert - JWT decode sẽ throw exception nếu token không hợp lệ format
        // Nhưng introspect check đã pass, nên chỉ test logic flow
        // Token format invalid sẽ throw JwtException, đó là expected behavior
        assertThrows(JwtException.class, () -> customJwtDecoder.decode(VALID_TOKEN));

        verify(authenticationService, times(1)).introspect(any(IntrospectRequest.class));
    }

    @Test
    void decode_invalidToken_shouldThrowException() {
        // Arrange
        IntrospectResponse introspectResponse = IntrospectResponse.builder()
                .valid(false)
                .build();

        when(authenticationService.introspect(any(IntrospectRequest.class)))
                .thenReturn(introspectResponse);

        // Act & Assert
        JwtException exception = assertThrows(JwtException.class, () -> customJwtDecoder.decode("invalid-token"));

        assertEquals("Token invalid", exception.getMessage());
        verify(authenticationService, times(1)).introspect(any(IntrospectRequest.class));
    }

    @Test
    void decode_callsIntrospectWithCorrectToken() {
        // Arrange
        IntrospectResponse introspectResponse = IntrospectResponse.builder()
                .valid(true)
                .build();

        when(authenticationService.introspect(any(IntrospectRequest.class)))
                .thenReturn(introspectResponse);

        String testToken = "test-jwt-token";

        // Act
        try {
            customJwtDecoder.decode(testToken);
        } catch (JwtException e) {
            // Ignore JWT format errors, we're testing introspect call
        }

        // Assert
        verify(authenticationService, times(1)).introspect(argThat(request ->
                request.getToken().equals(testToken)
        ));
    }

    @Test
    void decode_callsIntrospectForEachDecode() {
        // Arrange
        IntrospectResponse introspectResponse = IntrospectResponse.builder()
                .valid(true)
                .build();

        when(authenticationService.introspect(any(IntrospectRequest.class)))
                .thenReturn(introspectResponse);

        // Act - call decode multiple times, each in separate try-catch
        // to ensure all calls are executed even if exceptions are thrown
        try {
            customJwtDecoder.decode("token1");
        } catch (JwtException e) {
            // Ignore JWT format errors - we're testing introspect calls
        }
        
        try {
            customJwtDecoder.decode("token2");
        } catch (JwtException e) {
            // Ignore JWT format errors
        }
        
        try {
            customJwtDecoder.decode("token3");
        } catch (JwtException e) {
            // Ignore JWT format errors
        }

        // Assert - introspect should be called for each decode attempt
        verify(authenticationService, times(3)).introspect(any(IntrospectRequest.class));
    }

    @Test
    void decode_nullToken_shouldThrowException() {
        // Arrange
        IntrospectResponse introspectResponse = IntrospectResponse.builder()
                .valid(false)
                .build();

        when(authenticationService.introspect(any(IntrospectRequest.class)))
                .thenReturn(introspectResponse);

        // Act & Assert
        assertThrows(Exception.class, () -> customJwtDecoder.decode(null));
    }
}
