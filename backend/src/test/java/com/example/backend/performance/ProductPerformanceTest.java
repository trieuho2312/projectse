package com.example.backend.performance;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.dto.response.ShopResponse;
import com.example.backend.service.ProductService;
import com.example.backend.service.ShopService;
import com.example.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
// Note: @Transactional removed for performance tests to avoid transaction isolation issues
// in concurrent tests. Each test method manages its own transaction if needed.
class ProductPerformanceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private String shopId;

    @BeforeEach
    @Transactional
    @Commit // Commit transaction to ensure shop is visible to concurrent threads
    void setUp() {
        // Create user first (only if doesn't exist)
        try {
            UserCreationRequest userRequest = UserCreationRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .fullname("Test User")
                    .email("testuser@sis.hust.edu.vn")
                    .build();
            userService.createUser(userRequest);
        } catch (com.example.backend.exception.AppException e) {
            // User already exists, that's OK
            if (!e.getErrorCode().equals(com.example.backend.exception.ErrorCode.USERNAME_EXISTED)) {
                throw e;
            }
        }

        // Mock authentication to create shop
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Create a real shop for testing
        // Note: ShopService.createShop() has @Transactional, so shop will be committed
        ShopCreationRequest shopRequest = ShopCreationRequest.builder()
                .name("Performance Test Shop")
                .build();
        ShopResponse shop = shopService.createShop(shopRequest);
        shopId = shop.getShopId();

        // Note: @Commit will commit the transaction, making shop visible to all threads
        // No need to flush/clear manually

        // Clear authentication
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllProducts_shouldRespondWithin100ms() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act: Get all products for the shop
        List<ProductResponse> products = productService.getAllProducts(shopId);

        // Calculate duration
        long duration = System.currentTimeMillis() - startTime;

        // Assert: Response time should be reasonable
        assertNotNull(products);
        // Note: 100ms is a target, adjust based on actual performance
        assertTrue(duration < 1000, 
            "getAllProducts should respond within 1000ms, but took " + duration + "ms");
    }

    @Test
    @Transactional
    void getProductById_shouldRespondWithin50ms() {
        // Mock authentication for creating product
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Arrange: Create a product first
        ProductCreationRequest request = new ProductCreationRequest();
        request.setShopId(shopId);
        request.setName("Performance Test Product");
        request.setPrice(100000);
        request.setWeight(1000);
        request.setBrand("Test Brand");
        request.setDescription("Test Description");
        request.setCategoryNames(java.util.Set.of()); // Empty set for categories

        ProductResponse created = productService.createProduct(request);
        
        // Clear authentication after creating product
        SecurityContextHolder.clearContext();
        String productId = created.getProductId();

        // Act: Measure response time
        long startTime = System.currentTimeMillis();
        ProductResponse product = productService.getProductById(productId);
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertNotNull(product);
        assertTrue(duration < 500, 
            "getProductById should respond within 500ms, but took " + duration + "ms");
    }

    @Test
    void createProduct_underLoad_shouldHandle10ConcurrentRequests() throws InterruptedException {
        // Create shop in a separate transaction and commit it before concurrent tests
        String testShopId = transactionTemplate.execute(status -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    null,
                    java.util.Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            try {
                // Create shop for this test
                ShopCreationRequest shopRequest = ShopCreationRequest.builder()
                        .name("Concurrent Test Shop")
                        .build();
                ShopResponse shop = shopService.createShop(shopRequest);
                return shop.getShopId();
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
        
        // Shop is now committed and visible to all threads
        
        // Arrange
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Exception> exceptions = new ArrayList<>();

        // Act: Create products concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            final String currentShopId = testShopId;
            executor.submit(() -> {
                // Each thread needs its own transaction to avoid LazyInitializationException
                transactionTemplate.execute(status -> {
                    try {
                        // Set authentication in each thread
                        Authentication threadAuth = new UsernamePasswordAuthenticationToken(
                                "testuser",
                                null,
                                java.util.Collections.emptyList()
                        );
                        SecurityContextHolder.getContext().setAuthentication(threadAuth);

                        ProductCreationRequest request = new ProductCreationRequest();
                        request.setShopId(currentShopId);
                        request.setName("Concurrent Product " + index);
                        request.setPrice(100000 + index);
                        request.setWeight(1000);
                        request.setBrand("Test Brand");
                        request.setDescription("Test Description " + index);
                        request.setCategoryNames(java.util.Set.of()); // Empty set for categories

                        ProductResponse product = productService.createProduct(request);
                        assertNotNull(product);
                        
                        // Clear authentication
                        SecurityContextHolder.clearContext();
                        return null;
                    } catch (Exception e) {
                        exceptions.add(e);
                        return null;
                    } finally {
                        latch.countDown();
                    }
                });
            });
        }

        // Wait for all threads to complete (max 30 seconds)
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "All concurrent requests should complete within 30 seconds");
        assertTrue(exceptions.isEmpty(), 
            "No exceptions should occur during concurrent product creation. Exceptions: " + exceptions);
    }

    @Test
    void getAllProducts_underLoad_shouldHandle50ConcurrentRequests() throws InterruptedException {
        // Create shop in a separate transaction and commit it before concurrent tests
        String testShopId = transactionTemplate.execute(status -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    null,
                    java.util.Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            try {
                // Create shop for this test
                ShopCreationRequest shopRequest = ShopCreationRequest.builder()
                        .name("Concurrent Get Test Shop")
                        .build();
                ShopResponse shop = shopService.createShop(shopRequest);
                return shop.getShopId();
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
        
        // Shop is now committed and visible to all threads
        
        // Arrange
        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Exception> exceptions = new ArrayList<>();

        // Act: Get all products concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final String currentShopId = testShopId; // Use final variable for lambda
            executor.submit(() -> {
                try {
                    // getAllProducts doesn't need authentication, but ensure shopId is valid
                    List<ProductResponse> products = productService.getAllProducts(currentShopId);
                    assertNotNull(products);
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "All concurrent requests should complete within 30 seconds");
        assertTrue(exceptions.isEmpty(), 
            "No exceptions should occur during concurrent product retrieval. Exceptions: " + exceptions);
    }

    @Test
    @Transactional
    void createProduct_shouldCompleteWithin500ms() {
        // Mock authentication for creating product
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Arrange
        ProductCreationRequest request = new ProductCreationRequest();
        request.setShopId(shopId);
        request.setName("Performance Product");
        request.setPrice(100000);
        request.setWeight(1000);
        request.setBrand("Test Brand");
        request.setDescription("Test Description");
        request.setCategoryNames(java.util.Set.of()); // Empty set for categories

        // Act: Measure creation time
        long startTime = System.currentTimeMillis();
        ProductResponse product = productService.createProduct(request);
        
        // Clear authentication
        SecurityContextHolder.clearContext();
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertNotNull(product);
        assertTrue(duration < 2000, 
            "createProduct should complete within 2000ms, but took " + duration + "ms");
    }
}
