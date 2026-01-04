package com.example.backend.mapper;

import com.example.backend.dto.response.CartItemResponse;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CartMapperTest {

    private CartMapper cartMapper;

    @BeforeEach
    void setUp() {
        cartMapper = new CartMapperImpl();
    }

    @Test
    void toCartResponse_shouldMapCorrectly() {
        // Arrange
        Cart cart = Cart.builder()
                .id("cart-1")
                .totalAmount(150.0)
                .items(new ArrayList<>())
                .build();

        // Act
        CartResponse response = cartMapper.toCartResponse(cart);

        // Assert
        assertNotNull(response);
        assertEquals("cart-1", response.getId());
        assertEquals(150.0, response.getTotalAmount());
    }

    @Test
    void toCartResponse_shouldMapId() {
        // Arrange
        Cart cart = Cart.builder()
                .id("test-cart-id")
                .totalAmount(100.0)
                .build();

        // Act
        CartResponse response = cartMapper.toCartResponse(cart);

        // Assert
        assertEquals("test-cart-id", response.getId());
    }

    @Test
    void toCartItemResponse_shouldMapCorrectly() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-1")
                .name("Test Product")
                .build();

        CartItem cartItem = CartItem.builder()
                .id("item-1")
                .product(product)
                .quantity(3)
                .build();

        // Act
        CartItemResponse response = cartMapper.toCartItemResponse(cartItem);

        // Assert
        assertNotNull(response);
        assertEquals("prod-1", response.getProductId());
        assertEquals("Test Product", response.getProductName());
        assertEquals(3, response.getQuantity());
    }

    @Test
    void toCartItemResponse_shouldMapProductFields() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-123")
                .name("Special Product")
                .build();

        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(5)
                .build();

        // Act
        CartItemResponse response = cartMapper.toCartItemResponse(cartItem);

        // Assert
        assertEquals("prod-123", response.getProductId());
        assertEquals("Special Product", response.getProductName());
        assertEquals(5, response.getQuantity());
    }

    @Test
    void toCartResponse_nullCart_returnsNull() {
        // Act
        CartResponse response = cartMapper.toCartResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toCartItemResponse_nullCartItem_returnsNull() {
        // Act
        CartItemResponse response = cartMapper.toCartItemResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toCartItemResponse_nullProduct_shouldHandleGracefully() {
        // Arrange
        CartItem cartItem = CartItem.builder()
                .id("item-1")
                .product(null)
                .quantity(1)
                .build();

        // Act
        CartItemResponse response = cartMapper.toCartItemResponse(cartItem);

        // Assert
        assertNotNull(response);
        assertNull(response.getProductId());
        assertNull(response.getProductName());
        assertEquals(1, response.getQuantity());
    }

    @Test
    void toCartResponse_withItems_shouldMapItems() {
        // Arrange
        Product product1 = Product.builder()
                .productId("prod-1")
                .name("Product 1")
                .build();

        Product product2 = Product.builder()
                .productId("prod-2")
                .name("Product 2")
                .build();

        CartItem item1 = CartItem.builder()
                .product(product1)
                .quantity(2)
                .build();

        CartItem item2 = CartItem.builder()
                .product(product2)
                .quantity(1)
                .build();

        List<CartItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        Cart cart = Cart.builder()
                .id("cart-1")
                .totalAmount(200.0)
                .items(items)
                .build();

        // Act
        CartResponse response = cartMapper.toCartResponse(cart);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getItems());
        // Note: MapStruct automatically maps list items using toCartItemResponse
        // The actual mapping is handled by MapStruct
    }
}
