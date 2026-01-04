package com.example.backend.exception;

import com.example.backend.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleAppException_success() {
        // Arrange
        ErrorCode errorCode = ErrorCode.USER_NOT_EXIST;
        AppException ex = new AppException(errorCode);

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleAppException(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(errorCode.getStatus(), response.getStatusCode());
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(errorCode.getCode(), body.getCode());
        assertEquals(errorCode.getMessage(), body.getMessage());
        assertEquals("/api/test", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void handleAppException_differentErrorCodes() {
        // Arrange
        ErrorCode[] errorCodes = {
                ErrorCode.UNAUTHORIZED,
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.USERNAME_EXISTED,
                ErrorCode.PRODUCT_NOT_EXIST
        };

        for (ErrorCode errorCode : errorCodes) {
            AppException ex = new AppException(errorCode);

            // Act
            ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleAppException(ex, request);

            // Assert
            assertNotNull(response);
            assertEquals(errorCode.getStatus(), response.getStatusCode());
            ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
            assertEquals(errorCode.getCode(), body.getCode());
            assertEquals(errorCode.getMessage(), body.getMessage());
        }
    }

    @Test
    void handleValidation_success() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        
        FieldError fieldError1 = new FieldError("object", "field1", "Field1 is required");
        FieldError fieldError2 = new FieldError("object", "field2", "Field2 is invalid");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidation(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), body.getCode());
        assertTrue(body.getMessage().contains("Field1 is required"));
        assertTrue(body.getMessage().contains("Field2 is invalid"));
        assertEquals("/api/test", body.getPath());
    }

    @Test
    void handleValidation_singleError() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        
        FieldError fieldError = new FieldError("object", "field", "Field is required");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidation(ex, request);

        // Assert
        assertNotNull(response);
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), body.getCode());
        assertEquals("Field is required", body.getMessage());
    }

    @Test
    void handleMethodNotAllowed_success() {
        // Arrange
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST", Arrays.asList("GET", "PUT"));

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleMethodNotAllowed(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.getCode(), body.getCode());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.getMessage(), body.getMessage());
        assertEquals("/api/test", body.getPath());
    }

    @Test
    void handleJsonParse_success() {
        // Arrange
        HttpInputMessage inputMessage = Objects.requireNonNull(mock(HttpInputMessage.class));
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error: Unexpected character", new RuntimeException(), inputMessage);

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleJsonParse(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(ErrorCode.JSON_PARSE_ERROR.getCode(), body.getCode());
        assertEquals(ErrorCode.JSON_PARSE_ERROR.getMessage(), body.getMessage());
        assertEquals("/api/test", body.getPath());
    }

    @Test
    void handleOtherExceptions_success() {
        // Arrange
        RuntimeException ex = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleOtherExceptions(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), body.getCode());
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage(), body.getMessage());
        assertEquals("/api/test", body.getPath());
    }

    @Test
    void handleOtherExceptions_nullPointerException() {
        // Arrange
        NullPointerException ex = new NullPointerException("Null pointer");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleOtherExceptions(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), body.getCode());
    }

    @Test
    void handleOtherExceptions_illegalArgumentException() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleOtherExceptions(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), body.getCode());
    }

    @Test
    void buildResponse_withMessage() {
        // Arrange
        String customMessage = "Custom validation message";

        // Act - sử dụng reflection để gọi private method (hoặc test qua public method)
        // Vì buildResponse là private, ta test qua handleValidation với custom message
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        FieldError fieldError = new FieldError("object", "field", customMessage);
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidation(ex, request);

        // Assert
        assertNotNull(response);
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(customMessage, body.getMessage());
    }

    @Test
    void buildResponse_withoutMessage() {
        // Arrange
        ErrorCode errorCode = ErrorCode.USER_NOT_EXIST;
        AppException ex = new AppException(errorCode);

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleAppException(ex, request);

        // Assert - nếu không có custom message thì dùng message từ ErrorCode
        assertNotNull(response);
        ApiResponse<Object> body = Objects.requireNonNull(response.getBody());
        assertEquals(errorCode.getMessage(), body.getMessage());
    }

    @Test
    void allHandlers_setTimestamp() {
        // Arrange
        AppException appEx = new AppException(ErrorCode.USER_NOT_EXIST);
        RuntimeException runtimeEx = new RuntimeException("Test");
        HttpRequestMethodNotSupportedException methodEx = new HttpRequestMethodNotSupportedException("POST", List.of("GET"));
        HttpInputMessage inputMessage = Objects.requireNonNull(mock(HttpInputMessage.class));
        HttpMessageNotReadableException jsonEx = new HttpMessageNotReadableException("JSON error", new RuntimeException(), inputMessage);

        // Act
        ResponseEntity<ApiResponse<Object>> appResponse = globalExceptionHandler.handleAppException(appEx, request);
        ResponseEntity<ApiResponse<Object>> runtimeResponse = globalExceptionHandler.handleOtherExceptions(runtimeEx, request);
        ResponseEntity<ApiResponse<Object>> methodResponse = globalExceptionHandler.handleMethodNotAllowed(methodEx, request);
        ResponseEntity<ApiResponse<Object>> jsonResponse = globalExceptionHandler.handleJsonParse(jsonEx, request);

        // Assert - tất cả responses đều có timestamp
        assertNotNull(Objects.requireNonNull(appResponse.getBody()).getTimestamp());
        assertNotNull(Objects.requireNonNull(runtimeResponse.getBody()).getTimestamp());
        assertNotNull(Objects.requireNonNull(methodResponse.getBody()).getTimestamp());
        assertNotNull(Objects.requireNonNull(jsonResponse.getBody()).getTimestamp());
    }
}
