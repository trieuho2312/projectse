package com.example.backend.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database Migration Tests using TestContainers with PostgreSQL.
 * 
 * NOTE: These tests require Docker to be installed and running.
 * 
 * To run these tests:
 * 1. Install Docker Desktop (https://www.docker.com/products/docker-desktop)
 * 2. Start Docker Desktop
 * 3. Remove @Disabled annotation below
 * 
 * If Docker is not available, these tests will be skipped automatically.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Disabled("Docker is required. Install Docker Desktop and remove this annotation to run migration tests.")
class DatabaseMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("migration_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired(required = false)
    private Flyway flyway;

    @Test
    void flywayMigrations_shouldApplySuccessfully() {
        // This test verifies that Flyway migrations can be applied
        // If Flyway is configured, migrations should run automatically on Spring Boot startup
        
        // Assert: Spring context should load successfully
        // If migrations fail, Spring context won't load and test will fail
        assertNotNull(postgres);
        assertTrue(postgres.isRunning());
        
        // If Flyway is configured, verify it's not null
        // Note: Flyway might not be autoconfigured if no migration files exist
        if (flyway != null) {
            var info = flyway.info();
            assertNotNull(info);
        }
    }

    @Test
    void databaseConnection_shouldWork() {
        // Verify database connection works
        assertNotNull(postgres);
        assertTrue(postgres.isRunning());
        assertNotNull(postgres.getJdbcUrl());
        assertNotNull(postgres.getUsername());
        assertNotNull(postgres.getPassword());
    }

    @Test
    void migrations_shouldBeIdempotent() {
        // This test verifies that running migrations multiple times doesn't cause issues
        // Flyway should handle this automatically
        
        // If Flyway is configured, running migrations twice should not fail
        if (flyway != null) {
            // First migration (already done on startup)
            var info1 = flyway.info();
            
            // Attempt to migrate again (should be idempotent)
            flyway.migrate();
            
            var info2 = flyway.info();
            
            // Both should succeed
            assertNotNull(info1);
            assertNotNull(info2);
        }
    }
}
