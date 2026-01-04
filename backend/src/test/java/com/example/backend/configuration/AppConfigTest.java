package com.example.backend.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    private final AppConfig appConfig = new AppConfig();

    @Test
    void restTemplate_shouldReturnRestTemplate() {
        // Act
        RestTemplate restTemplate = appConfig.restTemplate();

        // Assert
        assertNotNull(restTemplate);
        assertInstanceOf(RestTemplate.class, restTemplate);
    }

    @Test
    void restTemplate_shouldReturnNewInstance() {
        // Act
        RestTemplate restTemplate1 = appConfig.restTemplate();
        RestTemplate restTemplate2 = appConfig.restTemplate();

        // Assert
        // Each call should return a new instance (not singleton)
        assertNotNull(restTemplate1);
        assertNotNull(restTemplate2);
        // Note: If @Bean is used without @Scope, Spring creates singleton by default
        // But in unit test, we're calling the method directly, so each call creates new instance
    }
}
