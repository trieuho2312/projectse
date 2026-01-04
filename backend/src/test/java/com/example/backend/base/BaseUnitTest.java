package com.example.backend.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class cho tất cả unit tests
 * Cung cấp common setup và utilities
 * <p>
 * Sử dụng: extends BaseUnitTest thay vì @ExtendWith(MockitoExtension.class)
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest {

    @BeforeEach
    void baseSetUp() {
        // Common setup cho tất cả unit tests
        // Override setUp() trong subclass nếu cần thêm setup
    }

    /**
     * Hook method để subclass override nếu cần custom setup
     */
    protected void setUp() {
        // Override trong subclass
    }
}
