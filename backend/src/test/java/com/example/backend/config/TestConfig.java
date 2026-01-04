package com.example.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

/**
 * Test Configuration class
 * Cung cấp các beans cấu hình cho testing
 * <p>
 * Lưu ý: Chỉ sử dụng trong test environment
 */
@TestConfiguration
public class TestConfig {

    /**
     * Password encoder cho testing (không encode, chỉ để test)
     * Trong production nên dùng BCryptPasswordEncoder
     * <p>
     * Note: NoOpPasswordEncoder is deprecated nhưng vẫn dùng được cho test
     * Có thể thay bằng BCryptPasswordEncoder với cost factor thấp cho test
     */
    @Bean
    @Primary
    @SuppressWarnings("deprecation")
    public PasswordEncoder testPasswordEncoder() {
        // Chỉ dùng cho test, không dùng trong production
        // NoOpPasswordEncoder deprecated nhưng vẫn OK cho unit tests
        return NoOpPasswordEncoder.getInstance();
    }
}
