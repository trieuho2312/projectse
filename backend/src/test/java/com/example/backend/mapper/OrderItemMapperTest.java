package com.example.backend.mapper;

import com.example.backend.dto.response.OrderItemResponse;
import com.example.backend.entity.OrderItem;
import com.example.backend.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderItemMapperTest {

    private OrderItemMapper orderItemMapper;

    @BeforeEach
    void setUp() {
        orderItemMapper = new OrderItemMapperImpl();
    }

    @Test
    void toOrderItemResponse_shouldMapCorrectly() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-1")
                .name("Test Product")
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id("item-1")
                .product(product)
                .quantity(3)
                .priceAtPurchase(99.99)
                .build();

        // Act
        OrderItemResponse response = orderItemMapper.toOrderItemResponse(orderItem);

        // Assert
        assertNotNull(response);
        assertEquals("prod-1", response.getProductId());
        assertEquals("Test Product", response.getProductName());
        assertEquals(3, response.getQuantity());
        assertEquals(99.99, response.getPriceAtPurchase());
    }

    @Test
    void toOrderItemResponse_shouldMapProductId() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-123")
                .name("Product Name")
                .build();

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(1)
                .priceAtPurchase(50.0)
                .build();

        // Act
        OrderItemResponse response = orderItemMapper.toOrderItemResponse(orderItem);

        // Assert
        assertEquals("prod-123", response.getProductId());
    }

    @Test
    void toOrderItemResponse_shouldMapProductName() {
        // Arrange
        Product product = Product.builder()
                .productId("prod-1")
                .name("Special Product")
                .build();

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(1)
                .priceAtPurchase(100.0)
                .build();

        // Act
        OrderItemResponse response = orderItemMapper.toOrderItemResponse(orderItem);

        // Assert
        assertEquals("Special Product", response.getProductName());
    }

    @Test
    void toOrderItemResponse_nullOrderItem_returnsNull() {
        // Act
        OrderItemResponse response = orderItemMapper.toOrderItemResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toOrderItemResponse_nullProduct_shouldHandleGracefully() {
        // Arrange
        OrderItem orderItem = OrderItem.builder()
                .id("item-1")
                .product(null)
                .quantity(1)
                .priceAtPurchase(50.0)
                .build();

        // Act
        OrderItemResponse response = orderItemMapper.toOrderItemResponse(orderItem);

        // Assert
        assertNotNull(response);
        assertNull(response.getProductId());
        assertNull(response.getProductName());
        assertEquals(1, response.getQuantity());
        assertEquals(50.0, response.getPriceAtPurchase());
    }
}
