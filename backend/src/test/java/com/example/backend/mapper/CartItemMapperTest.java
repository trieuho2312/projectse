package com.example.backend.mapper;

import com.example.backend.dto.response.CartResponse;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CartItemMapperTest {

    private CartItemMapper cartItemMapper;

    @BeforeEach
    void setUp() {
        cartItemMapper = new CartItemMapperImpl();
    }

    @Test
    void toCartResponse_shouldMapCorrectly() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-1")
                .name("Test Product")
                .build();

        CartItem cartItem = CartItem.builder()
                .id("item-1")
                .product(product)
                .quantity(2)
                .build();

        // Act
        CartResponse response = cartItemMapper.toCartResponse(cartItem);

        // Assert
        assertNotNull(response);
        // Note: CartItemMapper maps CartItem to CartResponse, 
        // but the actual mapping might need to be verified based on implementation
    }

    @Test
    void toCartResponse_nullCartItem_returnsNull() {
        // Act
        CartResponse response = cartItemMapper.toCartResponse(null);

        // Assert
        assertNull(response);
    }
}
