package com.example.backend.configuration;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = mock(AuthenticationException.class);
    }

    @Test
    void commence_setsCorrectStatus() throws Exception {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        assertEquals(ErrorCode.UNAUTHENTICATED.getStatus().value(), response.getStatus());
    }

    @Test
    void commence_setsCorrectContentType() throws Exception {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        assertEquals("application/json", response.getContentType());
    }

    @Test
    void commence_returnsCorrectApiResponse() throws Exception {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        MockHttpServletResponse mockResponse = (MockHttpServletResponse) response;
        String responseContent = mockResponse.getContentAsString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains(String.valueOf(ErrorCode.UNAUTHENTICATED.getCode())));
        assertTrue(responseContent.contains(ErrorCode.UNAUTHENTICATED.getMessage()));
    }

    @Test
    void commence_responseHasValidJsonFormat() throws Exception {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        MockHttpServletResponse mockResponse = (MockHttpServletResponse) response;
        String responseContent = mockResponse.getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Should be valid JSON
        assertDoesNotThrow(() -> {
            objectMapper.readValue(responseContent, ApiResponse.class);
        });
    }

    @Test
    void commence_responseContainsErrorCode() throws Exception {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        MockHttpServletResponse mockResponse = (MockHttpServletResponse) response;
        String responseContent = mockResponse.getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
        
        assertEquals(ErrorCode.UNAUTHENTICATED.getCode(), apiResponse.getCode());
        assertEquals(ErrorCode.UNAUTHENTICATED.getMessage(), apiResponse.getMessage());
    }

    @Test
    void commence_flushesBuffer() throws Exception {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert - response should be committed after flush
        // In MockHttpServletResponse, we can check if content was written
        MockHttpServletResponse mockResponse = (MockHttpServletResponse) response;
        assertFalse(mockResponse.getContentAsString().isEmpty());
    }

    @Test
    void commence_handlesIOException() throws Exception {
        // Arrange
        HttpServletResponse badResponse = mock(HttpServletResponse.class);
        doThrow(new IOException("Test exception")).when(badResponse).getWriter();

        // Act & Assert
        assertThrows(IOException.class, () -> jwtAuthenticationEntryPoint.commence(request, badResponse, authException));
    }
}
