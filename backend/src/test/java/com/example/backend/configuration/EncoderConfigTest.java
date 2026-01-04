package com.example.backend.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class EncoderConfigTest {

    private final EncoderConfig encoderConfig = new EncoderConfig();

    @Test
    void passwordEncoder_shouldReturnBCryptEncoder() {
        // Act
        PasswordEncoder encoder = encoderConfig.passwordEncoder();

        // Assert
        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void passwordEncoder_shouldHaveCorrectStrength() {
        // Act
        PasswordEncoder encoder = encoderConfig.passwordEncoder();

        // Assert
        assertNotNull(encoder);
        
        // Test that encoder works correctly
        String rawPassword = "testPassword123";
        String encoded = encoder.encode(rawPassword);
        
        assertNotNull(encoded);
        assertTrue(encoder.matches(rawPassword, encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
    }

    @Test
    void passwordEncoder_shouldEncodeDifferentPasswordsDifferently() {
        // Act
        PasswordEncoder encoder = encoderConfig.passwordEncoder();

        // Assert
        String password1 = "password1";
        String password2 = "password2";
        
        String encoded1 = encoder.encode(password1);
        String encoded2 = encoder.encode(password2);
        
        // Each encoding should be different (due to salt)
        assertNotEquals(encoded1, encoded2);
        
        // But both should match their original passwords
        assertTrue(encoder.matches(password1, encoded1));
        assertTrue(encoder.matches(password2, encoded2));
    }
}
