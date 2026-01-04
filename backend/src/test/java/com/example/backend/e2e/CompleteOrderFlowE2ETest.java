package com.example.backend.e2e;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E Tests using TestContainers with PostgreSQL.
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
@Transactional
@Disabled("Docker is required. Install Docker Desktop and remove this annotation to run E2E tests.")
class CompleteOrderFlowE2ETest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private UserResponse buyer;
    private ShopResponse shop;
    private ProductResponse product1;
    private ProductResponse product2;

    @BeforeEach
    void setUp() {
        // Create buyer
        UserCreationRequest buyerRequest = UserCreationRequest.builder()
                .username("e2ebuyer")
                .password("password123")
                .fullname("E2E Buyer")
                .email("e2ebuyer@sis.hust.edu.vn")
                .build();
        buyer = userService.createUser(buyerRequest);

        // Create seller
        UserCreationRequest sellerRequest = UserCreationRequest.builder()
                .username("e2eseller")
                .password("password123")
                .fullname("E2E Seller")
                .email("e2eseller@sis.hust.edu.vn")
                .build();
        userService.createUser(sellerRequest);

        // Create category
        CategoryCreationRequest categoryRequest = CategoryCreationRequest.builder()
                .name("E2E Category")
                .build();
        CategoryResponse category = categoryService.createCategory(categoryRequest);

        // Mock authentication for seller to create shop
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "e2eseller",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Create shop
        ShopCreationRequest shopRequest = ShopCreationRequest.builder()
                .name("E2E Test Shop")
                .build();
        shop = shopService.createShop(shopRequest);
        
        // Clear authentication
        SecurityContextHolder.clearContext();

        // Mock authentication for seller to create products
        Authentication sellerAuth = new UsernamePasswordAuthenticationToken(
                "e2eseller",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(sellerAuth);

        // Create products
        ProductCreationRequest productRequest1 = new ProductCreationRequest();
        productRequest1.setShopId(shop.getShopId());
        productRequest1.setName("E2E Product 1");
        productRequest1.setPrice(100000);
        productRequest1.setWeight(1000);
        productRequest1.setBrand("E2E Brand");
        productRequest1.setDescription("E2E Description 1");
        productRequest1.setCategoryNames(java.util.Set.of(category.getName()));
        product1 = productService.createProduct(productRequest1);

        ProductCreationRequest productRequest2 = new ProductCreationRequest();
        productRequest2.setShopId(shop.getShopId());
        productRequest2.setName("E2E Product 2");
        productRequest2.setPrice(200000);
        productRequest2.setWeight(2000);
        productRequest2.setBrand("E2E Brand");
        productRequest2.setDescription("E2E Description 2");
        productRequest2.setCategoryNames(java.util.Set.of(category.getName()));
        product2 = productService.createProduct(productRequest2);
        
        // Clear authentication
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeE2EOrderFlow_shouldWorkEndToEnd() {
        // Step 1: Verify users exist in database
        Optional<User> buyerEntity = userRepository.findByUsername("e2ebuyer");
        assertTrue(buyerEntity.isPresent());
        assertEquals("E2E Buyer", buyerEntity.get().getFullname());

        // Mock authentication for buyer to add to cart
        Authentication buyerAuth = new UsernamePasswordAuthenticationToken(
                "e2ebuyer",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(buyerAuth);

        // Step 2: Add products to cart
        cartService.addToCart(new CartRequest(product1.getProductId(), 2), buyer.getUserId());
        cartService.addToCart(new CartRequest(product2.getProductId(), 1), buyer.getUserId());
        
        // Keep authentication for getCartByUser

        // Step 3: Verify cart in database
        Optional<Cart> cartEntity = cartRepository.findByUser(buyerEntity.get());
        assertTrue(cartEntity.isPresent());
        assertEquals(2, cartEntity.get().getItems().size());

        // Step 4: Get cart
        CartResponse cart = cartService.getCartByUser(buyer.getUserId());
        assertNotNull(cart);
        assertEquals(2, cart.getItems().size());
        assertTrue(cart.getTotalAmount() > 0);

        // Step 5: Get cart item IDs for checkout
        List<String> cartItemIds = cartEntity.get().getItems().stream()
                .map(CartItem::getId)
                .toList();

        assertFalse(cartItemIds.isEmpty());

        // Step 6: Checkout (create order) - authentication still set from buyer
        // Get product IDs from cart items
        List<String> productIds = cartEntity.get().getItems().stream()
                .map(item -> item.getProduct().getProductId())
                .toList();
        
        OrderSelectedItemsRequest checkoutRequest = new OrderSelectedItemsRequest();
        checkoutRequest.setProductIds(productIds);

        List<OrderResponse> orders = orderService.checkoutSelectedItems(checkoutRequest);
        
        // Clear authentication after checkout
        SecurityContextHolder.clearContext();

        // Step 7: Verify orders created
        assertNotNull(orders);
        assertFalse(orders.isEmpty());

        OrderResponse order = orders.get(0);
        assertNotNull(order.getOrderId());
        assertTrue(order.getTotalAmount() > 0);
        assertNotNull(order.getItems());
        assertEquals(2, order.getItems().size());

        // Step 8: Verify order in database
        Optional<Order> dbOrder = orderRepository.findById(order.getOrderId());
        assertTrue(dbOrder.isPresent());
        assertEquals(buyer.getUserId(), dbOrder.get().getUser().getUserId());
        assertEquals(2, dbOrder.get().getItems().size());

        // Step 9: Verify order items have correct data
        OrderItem item1 = dbOrder.get().getItems().get(0);
        assertNotNull(item1.getProduct());
        assertTrue(item1.getQuantity() > 0);
        assertTrue(item1.getPriceAtPurchase() > 0);

        // Step 10: Verify cart is cleared or updated after checkout
        // (Depending on business logic)
    }

    @Test
    void createUser_thenCreateShop_thenCreateProduct_shouldWork() {
        // This test verifies the complete flow of creating user, shop, and product
        // All using real database through TestContainers

        // Step 1: Create user
        UserCreationRequest userRequest = UserCreationRequest.builder()
                .username("flowuser")
                .password("password123")
                .fullname("Flow User")
                .email("flow@sis.hust.edu.vn")
                .build();
        userService.createUser(userRequest);

        // Verify in database
        Optional<User> dbUser = userRepository.findByUsername("flowuser");
        assertTrue(dbUser.isPresent());

        // Mock authentication for user to create shop
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "flowuser",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Step 2: Create shop
        ShopCreationRequest shopRequest = ShopCreationRequest.builder()
                .name("Flow Shop")
                .build();
        ShopResponse shop = shopService.createShop(shopRequest);
        
        // Clear authentication
        SecurityContextHolder.clearContext();

        assertNotNull(shop);
        assertNotNull(shop.getShopId());

        // Mock authentication for user to create product
        Authentication productAuth = new UsernamePasswordAuthenticationToken(
                "flowuser",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(productAuth);

        // Step 3: Create product
        ProductCreationRequest productRequest = new ProductCreationRequest();
        productRequest.setShopId(shop.getShopId());
        productRequest.setName("Flow Product");
        productRequest.setPrice(50000);
        productRequest.setWeight(500);
        productRequest.setBrand("Flow Brand");
        productRequest.setDescription("Flow Description");
        ProductResponse product = productService.createProduct(productRequest);
        
        // Clear authentication
        SecurityContextHolder.clearContext();

        assertNotNull(product);
        assertEquals("Flow Product", product.getName());
        assertEquals(shop.getShopId(), product.getShopId());
    }
}
