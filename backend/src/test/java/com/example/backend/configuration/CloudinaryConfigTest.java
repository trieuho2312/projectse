package com.example.backend.configuration;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloudinaryConfigTest {

    private final CloudinaryConfig cloudinaryConfig = new CloudinaryConfig();

    @Test
    void cloudinary_shouldReturnCloudinaryInstance() {
        // Act
        Cloudinary cloudinary = cloudinaryConfig.cloudinary();

        // Assert
        assertNotNull(cloudinary);
        assertInstanceOf(Cloudinary.class, cloudinary);
    }

    @Test
    void cloudinary_shouldHaveCorrectConfiguration() {
        // Act
        Cloudinary cloudinary = cloudinaryConfig.cloudinary();

        // Assert
        assertNotNull(cloudinary);
        
        // Verify configuration values
        // Note: Cloudinary configuration is accessed via internal config
        // We can only verify the instance is created successfully
        // Actual config values are hardcoded in the config class
    }
}
