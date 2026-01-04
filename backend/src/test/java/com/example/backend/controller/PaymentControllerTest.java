package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.PaymentSimulationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    PaymentSimulationService paymentService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    void payOnline_success() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("order-1");
        request.setPaymentMethod("VNPAY");

        PaymentResponse response = PaymentResponse.builder()
                .orderId("order-1")
                .amount(200000)
                .status("SUCCESS")
                .transactionId("payment-1")
                .build();

        when(paymentService.simulateOnlinePayment(any()))
                .thenReturn(response);

        mockMvc.perform(post("/payments/online")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.transactionId").value("payment-1"))
                .andExpect(jsonPath("$.result.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "user1")
    void createCOD_success() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .orderId("order-1")
                .amount(200000)
                .status("PENDING")
                .transactionId("payment-1")
                .build();

        when(paymentService.createCODPayment("order-1"))
                .thenReturn(response);

        mockMvc.perform(post("/payments/cod/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.transactionId").value("payment-1"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void confirmCOD_success() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .orderId("order-1")
                .amount(200000)
                .status("CONFIRMED")
                .transactionId("payment-1")
                .build();

        when(paymentService.confirmCODPayment("order-1"))
                .thenReturn(response);

        mockMvc.perform(post("/payments/cod/confirm/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("CONFIRMED"));
    }
}
