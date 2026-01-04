package com.example.backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppExceptionTest {

    @Test
    void constructor_shouldSetErrorCodeAndMessage() {
        // Arrange
        ErrorCode errorCode = ErrorCode.USER_NOT_EXIST;

        // Act
        AppException exception = new AppException(errorCode);

        // Assert
        assertNotNull(exception);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getMessage(), exception.getMessage());
    }

    @Test
    void constructor_withDifferentErrorCodes_shouldSetCorrectly() {
        // Test multiple error codes
        ErrorCode[] errorCodes = {
                ErrorCode.UNAUTHORIZED,
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.USERNAME_EXISTED,
                ErrorCode.PRODUCT_NOT_EXIST,
                ErrorCode.UNCATEGORIZED_EXCEPTION
        };

        for (ErrorCode errorCode : errorCodes) {
            // Act
            AppException exception = new AppException(errorCode);

            // Assert
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(errorCode.getMessage(), exception.getMessage());
        }
    }

    @Test
    void getErrorCode_shouldReturnSetErrorCode() {
        // Arrange
        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;
        AppException exception = new AppException(errorCode);

        // Act
        ErrorCode result = exception.getErrorCode();

        // Assert
        assertEquals(errorCode, result);
    }

    @Test
    void setErrorCode_shouldUpdateErrorCode() {
        // Arrange
        AppException exception = new AppException(ErrorCode.USER_NOT_EXIST);
        ErrorCode newErrorCode = ErrorCode.PRODUCT_NOT_EXIST;

        // Act
        exception.setErrorCode(newErrorCode);

        // Assert
        assertEquals(newErrorCode, exception.getErrorCode());
    }

    @Test
    void exception_shouldBeInstanceOfRuntimeException() {
        // Arrange
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        // Act
        AppException exception = new AppException(errorCode);

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void exception_message_shouldMatchErrorCodeMessage() {
        // Arrange
        ErrorCode errorCode = ErrorCode.EMAIL_EXISTED;

        // Act
        AppException exception = new AppException(errorCode);

        // Assert
        assertEquals("Email already existed", exception.getMessage());
    }
}
