package com.example.backend.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Load .env file before Spring Boot reads application.properties
 * This allows Spring Boot to read from .env file using ${} syntax
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> envMap = new HashMap<>();

            // Load all .env entries into Spring environment
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                envMap.put(key, value);
                // Also set as system property for backward compatibility
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            });

            // Add .env properties to Spring environment
            if (!envMap.isEmpty()) {
                environment.getPropertySources()
                        .addFirst(new MapPropertySource("dotenv", envMap));
            }
        } catch (Exception e) {
            // If .env file doesn't exist, just continue with default values
            System.out.println("Warning: .env file not found, using default values from application.properties");
        }
    }
}
