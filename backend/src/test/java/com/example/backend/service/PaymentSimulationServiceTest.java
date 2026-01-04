package com.example.backend.service;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.entity.Order;
import com.example.backend.entity.Payment;
import com.example.backend.enums.OrderStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PaymentSimulationServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    PaymentSimulationService paymentSimulationService;

    private Order testOrder;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id("order-1")
                .totalAmount(100000.0)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId("order-1");
        paymentRequest.setPaymentMethod("VNPay");
    }

    @Test
    @SuppressWarnings("null")
    void simulateOnlinePayment_success() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(paymentCaptor.capture())).thenAnswer(invocation -> {
            Payment payment = Objects.requireNonNull(paymentCaptor.getValue());
            payment.setId("payment-1");
            return payment;
        });
        // Use lenient() because orderRepository.save() is only called when payment is SUCCESS (90% chance)
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        lenient().when(orderRepository.save(orderCaptor.capture())).thenAnswer(invocation -> {
            Order order = Objects.requireNonNull(orderCaptor.getValue());
            return order;
        });

        PaymentResponse response = paymentSimulationService.simulateOnlinePayment(paymentRequest);

        assertNotNull(response);
        assertEquals("order-1", response.getOrderId());
        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN_"));
        assertEquals(100000.0, response.getAmount());
        assertEquals("VNPay", response.getPaymentMethod());
        assertNotNull(response.getStatus());
        assertTrue(response.getStatus().equals("SUCCESS") || response.getStatus().equals("FAILED"));

        verify(orderRepository).findById("order-1");
        verify(paymentRepository).save(any(Payment.class));
        if (response.getStatus().equals("SUCCESS")) {
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Test
    @SuppressWarnings("null")
    void simulateOnlinePayment_orderNotExist() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            paymentSimulationService.simulateOnlinePayment(paymentRequest);
        });

        assertEquals(ErrorCode.ORDER_NOT_EXIST, exception.getErrorCode());
        verify(orderRepository).findById("order-1");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @SuppressWarnings("null")
    void createCODPayment_success() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(paymentCaptor.capture())).thenAnswer(invocation -> {
            Payment payment = Objects.requireNonNull(paymentCaptor.getValue());
            payment.setId("payment-1");
            return payment;
        });

        PaymentResponse response = paymentSimulationService.createCODPayment("order-1");

        assertNotNull(response);
        assertEquals("order-1", response.getOrderId());
        assertEquals("COD_order-1", response.getTransactionId());
        assertEquals("PENDING", response.getStatus());
        assertEquals(100000.0, response.getAmount());
        assertEquals("COD", response.getPaymentMethod());
        assertEquals("Thanh toán khi nhận hàng", response.getMessage());

        verify(orderRepository).findById("order-1");
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository, never()).save(any(Order.class)); // Order status không đổi
    }

    @Test
    @SuppressWarnings("null")
    void createCODPayment_orderNotExist() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            paymentSimulationService.createCODPayment("order-1");
        });

        assertEquals(ErrorCode.ORDER_NOT_EXIST, exception.getErrorCode());
        verify(orderRepository).findById("order-1");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @SuppressWarnings("null")
    void confirmCODPayment_success() {
        Payment existingPayment = Payment.builder()
                .id("payment-1")
                .order(testOrder)
                .paymentMethod("COD")
                .paymentStatus("PENDING")
                .transactionId("COD_order-1")
                .amount(100000.0)
                .build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrder_Id("order-1")).thenReturn(Optional.of(existingPayment));
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenAnswer(invocation -> {
            Order order = Objects.requireNonNull(orderCaptor.getValue());
            return order;
        });

        PaymentResponse response = paymentSimulationService.confirmCODPayment("order-1");

        assertNotNull(response);
        assertEquals("order-1", response.getOrderId());
        assertEquals("COD_order-1", response.getTransactionId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(100000.0, response.getAmount());
        assertEquals("COD", response.getPaymentMethod());
        assertEquals("Đã xác nhận thanh toán COD", response.getMessage());

        verify(orderRepository).findById("order-1");
        verify(paymentRepository).findByOrder_Id("order-1");
        // Payment is updated in place (setPaymentStatus, setPaymentDate)
        // Note: In the actual service, payment is not explicitly saved, 
        // but it's managed by JPA and will be persisted on transaction commit
        verify(orderRepository).save(any(Order.class));
        assertEquals(OrderStatus.PAID, testOrder.getStatus());
        assertEquals("SUCCESS", existingPayment.getPaymentStatus());
    }

    @Test
    void confirmCODPayment_orderNotExist() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            paymentSimulationService.confirmCODPayment("order-1");
        });

        assertEquals(ErrorCode.ORDER_NOT_EXIST, exception.getErrorCode());
        verify(orderRepository).findById("order-1");
        verify(paymentRepository, never()).findByOrder_Id(anyString());
    }

    @Test
    void confirmCODPayment_paymentNotFound() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrder_Id("order-1")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentSimulationService.confirmCODPayment("order-1");
        });

        assertEquals("Payment not found", exception.getMessage());
        verify(orderRepository).findById("order-1");
        verify(paymentRepository).findByOrder_Id("order-1");
    }
}
