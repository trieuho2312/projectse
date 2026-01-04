package com.example.backend.service;

import com.example.backend.dto.request.CartRequest;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.Cart;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.CartMapper;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductImageRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Các unit test này được tạo với mục đích FAIL để demo/test
 * Tất cả các test trong file này sẽ fail một cách có chủ ý
 */
@ExtendWith(MockitoExtension.class)
class FailingUnitTests {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    com.example.backend.repository.RoleRepository roleRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductMapper productMapper;

    @Mock
    CartRepository cartRepository;

    @Mock
    CartMapper cartMapper;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ProductImageRepository productImageRepository;

    @Mock
    ProductImageService productImageService;

    @Mock
    com.example.backend.repository.ShopRepository shopRepository;

    @InjectMocks
    UserService userService;

    @InjectMocks
    ProductService productService;

    @InjectMocks
    CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .email("test@sis.hust.edu.vn")
                .fullname("Test User")
                .build();

        testProduct = Product.builder()
                .productId("product-1")
                .name("Test Product")
                .price(100000)
                .build();

        testCart = Cart.builder()
                .id("cart-1")
                .user(testUser)
                .totalAmount(50000)
                .build();
    }

    /**
     * Test này sẽ FAIL vì assert sai giá trị
     * Thực tế username là "testuser" nhưng test expect "wronguser"
     */
    @Test
    void testUserService_getUserById_wrongExpectedValue() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            UserResponse response = UserResponse.builder()
                    .userId("user-1")
                    .username("testuser")
                    .email("test@sis.hust.edu.vn")
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponse(testUser)).thenReturn(response);

            UserResponse result = userService.getUserById("user-1");

            // SAI: Expect "wronguser" nhưng thực tế là "testuser"
            assertEquals("wronguser", result.getUsername(), "Test này sẽ fail vì username sai");
        }
    }

    /**
     * Test này sẽ FAIL vì expect exception nhưng method không throw
     */
    @Test
    void testUserService_getUserById_expectExceptionButNotThrown() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            UserResponse response = UserResponse.builder()
                    .userId("user-1")
                    .username("testuser")
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponse(testUser)).thenReturn(response);

            // SAI: Expect exception nhưng method sẽ return thành công
            assertThrows(AppException.class, () -> {
                userService.getUserById("user-1");
            }, "Test này sẽ fail vì không có exception được throw");
        }
    }

    /**
     * Test này sẽ FAIL vì assert null nhưng object không null
     */
    @Test
    void testUserService_getUserById_expectNullButNotNull() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            UserResponse response = UserResponse.builder()
                    .userId("user-1")
                    .username("testuser")
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponse(testUser)).thenReturn(response);

            UserResponse result = userService.getUserById("user-1");

            // SAI: Expect null nhưng result không null
            assertNull(result, "Test này sẽ fail vì result không null");
        }
    }

    /**
     * Test này sẽ FAIL vì assert sai error code
     */
    @Test
    void testUserService_createUser_wrongErrorCode() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("existinguser")
                .password("12345678")
                .email("new@sis.hust.edu.vn")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> {
            userService.createUser(request);
        });

        // SAI: Expect EMAIL_EXISTED nhưng thực tế là USERNAME_EXISTED
        assertEquals(ErrorCode.EMAIL_EXISTED, exception.getErrorCode(), 
                "Test này sẽ fail vì error code sai");
    }

    /**
     * Test này sẽ FAIL vì assert sai số lượng
     */
    @Test
    void testProductService_getProductById_wrongPrice() {
        ProductResponse response = ProductResponse.builder()
                .productId("product-1")
                .name("Test Product")
                .price(100000)
                .build();

        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(productMapper.toProductResponse(testProduct)).thenReturn(response);

        ProductResponse result = productService.getProductById("product-1");

        // SAI: Expect price = 200000 nhưng thực tế là 100000
        assertEquals(200000, result.getPrice(), 
                "Test này sẽ fail vì price sai");
    }

    /**
     * Test này sẽ FAIL vì expect true nhưng thực tế là false
     */
    @Test
    void testCartService_addToCart_wrongBooleanAssertion() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");

            CartRequest request = CartRequest.builder()
                    .productId("product-1")
                    .quantity(2)
                    .build();

            CartResponse response = CartResponse.builder()
                    .id("cart-1")
                    .totalAmount(200000)
                    .build();

            testUser.setCart(null); // Ensure cart is null initially
            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.saveAndFlush(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartMapper.toCartResponse(any())).thenReturn(response);

            CartResponse result = cartService.addToCart(request, "user-1");

            // SAI: Expect true nhưng result không phải boolean
            assertTrue(result == null, 
                    "Test này sẽ fail vì assertion sai logic");
        }
    }

    /**
     * Test này sẽ FAIL vì so sánh string không đúng
     */
    @Test
    void testUserService_getUserById_wrongStringComparison() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            UserResponse response = UserResponse.builder()
                    .userId("user-1")
                    .username("testuser")
                    .email("test@sis.hust.edu.vn")
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponse(testUser)).thenReturn(response);

            UserResponse result = userService.getUserById("user-1");

            // SAI: Expect "TESTUSER" (uppercase) nhưng thực tế là "testuser" (lowercase)
            assertEquals("TESTUSER", result.getUsername(), 
                    "Test này sẽ fail vì case-sensitive string comparison");
        }
    }

    /**
     * Test này sẽ FAIL vì expect exception với message sai
     */
    @Test
    void testUserService_createUser_wrongExceptionMessage() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("newuser")
                .password("12345678")
                .email("invalid@gmail.com")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> {
            userService.createUser(request);
        });

        // SAI: Expect message "Username already exists" nhưng thực tế là message khác
        assertEquals("Username already exists", exception.getMessage(), 
                "Test này sẽ fail vì exception message sai");
    }

    /**
     * Test này sẽ FAIL vì assert sai số lượng items
     */
    @Test
    void testCartService_addToCart_wrongItemCount() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");

            CartRequest request = CartRequest.builder()
                    .productId("product-1")
                    .quantity(1)
                    .build();

            CartResponse response = CartResponse.builder()
                    .id("cart-1")
                    .totalAmount(100000)
                    .build();

            testUser.setCart(null); // Ensure cart is null initially
            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartRepository.saveAndFlush(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cartMapper.toCartResponse(any())).thenReturn(response);

            CartResponse result = cartService.addToCart(request, "user-1");

            // SAI: Giả sử response có items list, expect 5 items nhưng thực tế có thể khác
            // (Test này có thể fail vì logic assertion sai)
            assertNotNull(result);
            if (result.getItems() != null) {
                assertEquals(5, result.getItems().size(), 
                        "Test này sẽ fail vì số lượng items sai");
            } else {
                // Nếu items null thì cũng fail
                fail("Items list is null, test will fail");
            }
        }
    }

    /**
     * Test này sẽ FAIL vì verify sai số lần gọi method
     */
    @Test
    void testUserService_createUser_wrongVerifyCount() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("newuser")
                .password("12345678")
                .email("newuser@sis.hust.edu.vn")
                .build();

        User newUser = User.builder()
                .username("newuser")
                .email("newuser@sis.hust.edu.vn")
                .build();

        UserResponse response = UserResponse.builder()
                .userId("user-2")
                .username("newuser")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@sis.hust.edu.vn")).thenReturn(false);
        when(userMapper.toUser(any())).thenReturn(newUser);
        when(userRepository.save(any())).thenReturn(newUser);
        when(userMapper.toUserResponse(any())).thenReturn(response);
        // Mock các dependencies khác...

        // Gọi method trước
        userService.createUser(request);

        // SAI: Verify save được gọi 3 lần nhưng thực tế chỉ gọi 1 lần
        verify(userRepository, times(3)).save(any());
    }
}
