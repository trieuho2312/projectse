package com.example.backend.mapper;

import com.example.backend.dto.response.OrderResponse;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderItem;
import com.example.backend.entity.Product;
import com.example.backend.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    private OrderMapper orderMapper;
    private OrderItemMapper orderItemMapper;

    @BeforeEach
    void setUp() {
        orderItemMapper = new OrderItemMapperImpl();
        orderMapper = new OrderMapperImpl();
        // Inject OrderItemMapper into OrderMapper using reflection
        org.springframework.test.util.ReflectionTestUtils.setField(orderMapper, "orderItemMapper", orderItemMapper);
    }

    @Test
    void toOrderResponse_shouldMapCorrectly() {
        // Arrange
        Order order = Order.builder()
                .id("order-1")
                .totalAmount(199.99)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        OrderResponse response = orderMapper.toOrderResponse(order);

        // Assert
        assertNotNull(response);
        assertEquals("order-1", response.getOrderId());
        assertEquals(199.99, response.getTotalAmount());
    }

    @Test
    void toOrderResponse_shouldMapOrderIdFromId() {
        // Arrange
        Order order = Order.builder()
                .id("test-order-id")
                .totalAmount(100.0)
                .build();

        // Act
        OrderResponse response = orderMapper.toOrderResponse(order);

        // Assert
        assertEquals("test-order-id", response.getOrderId());
    }

    @Test
    void toOrderResponse_shouldMapOrderItems() {
        // Arrange
        Product product1 = Product.builder()
                .productId("prod-1")
                .name("Product 1")
                .build();

        Product product2 = Product.builder()
                .productId("prod-2")
                .name("Product 2")
                .build();

        OrderItem item1 = OrderItem.builder()
                .id("item-1")
                .product(product1)
                .quantity(2)
                .priceAtPurchase(50.0)
                .build();

        OrderItem item2 = OrderItem.builder()
                .id("item-2")
                .product(product2)
                .quantity(1)
                .priceAtPurchase(100.0)
                .build();

        List<OrderItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        Order order = Order.builder()
                .id("order-1")
                .totalAmount(200.0)
                .items(items)
                .build();

        // Act
        OrderResponse response = orderMapper.toOrderResponse(order);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getItems());
        assertEquals(2, response.getItems().size());
        assertEquals("prod-1", response.getItems().get(0).getProductId());
        assertEquals("Product 1", response.getItems().get(0).getProductName());
        assertEquals("prod-2", response.getItems().get(1).getProductId());
        assertEquals("Product 2", response.getItems().get(1).getProductName());
    }

    @Test
    void toOrderResponse_nullOrder_returnsNull() {
        // Act
        OrderResponse response = orderMapper.toOrderResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toOrderResponse_emptyItems_shouldHandleGracefully() {
        // Arrange
        Order order = Order.builder()
                .id("order-1")
                .totalAmount(0.0)
                .items(new ArrayList<>())
                .build();

        // Act
        OrderResponse response = orderMapper.toOrderResponse(order);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getItems());
        assertTrue(response.getItems().isEmpty());
    }
}
