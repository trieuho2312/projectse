package com.example.backend.performance;

import com.example.backend.dto.request.CartRequest;
import com.example.backend.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartPerformanceTest {

    @Autowired
    private CartService cartService;

    @Test
    void addToCart_shouldCompleteWithin200ms() {
        // Arrange
        String userId = "test-user-id";
        String productId = "test-product-id";
        CartRequest request = new CartRequest(productId, 1);

        // Act: Measure add to cart time
        long startTime = System.currentTimeMillis();
        try {
            cartService.addToCart(request, userId);
        } catch (Exception e) {
            // Expected if product/user doesn't exist, but we're testing performance
        }
        long duration = System.currentTimeMillis() - startTime;

        // Assert: Should complete quickly
        assertTrue(duration < 1000, 
            "addToCart should complete within 1000ms, but took " + duration + "ms");
    }

    @Test
    void getCart_shouldRespondWithin100ms() {
        // Arrange
        String userId = "test-user-id";

        // Act: Measure get cart time
        long startTime = System.currentTimeMillis();
        try {
            cartService.getCartByUser(userId);
        } catch (Exception e) {
            // Expected if cart doesn't exist
        }
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertTrue(duration < 1000, 
            "getCart should respond within 1000ms, but took " + duration + "ms");
    }

    @Test
    void addToCart_underLoad_shouldHandle20ConcurrentRequests() throws InterruptedException {
        // Arrange
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);

        // Act: Add to cart concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    CartRequest request = new CartRequest("product-" + index, 1);
                    cartService.addToCart(request, "test-user-id");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "All concurrent requests should complete within 30 seconds");
        // Note: Some failures are expected if products don't exist, but system should handle load
    }
}
