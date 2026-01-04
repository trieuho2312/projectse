package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test để verify Spring Application Context loads successfully
 * 
 * Test này đảm bảo:
 * - Tất cả beans được configure đúng
 * - Không có circular dependencies
 * - Configuration classes được load thành công
 * - Database connection (nếu có) được thiết lập đúng
 * 
 * Chạy test này để verify rằng application có thể khởi động được
 */
@SpringBootTest(properties = {
    "spring.flyway.enabled=false"
})
class ApplicationContextTest {

    /**
     * Test để verify Spring context loads successfully
     * Nếu test này pass, nghĩa là application configuration đúng
     */
    @Test
    void contextLoads() {
        // Test passes if Spring context loads without errors
        // Verify rằng tất cả beans được configure đúng
    }
}
