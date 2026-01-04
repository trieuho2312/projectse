package com.example.backend.controller;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.request.PayOSPaymentRequest;
import com.example.backend.dto.request.PayOSTestRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.dto.response.PayOSPaymentResponse;
import com.example.backend.service.PaymentSimulationService;
import com.example.backend.service.PayOSService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentSimulationService paymentService;
    PayOSService payOSService;

    /**
     * Giả lập thanh toán online (VNPay, Momo)
     * POST /payments/online
     */
    @PostMapping("/online")
    public ApiResponse<PaymentResponse> payOnline(@RequestBody PaymentRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.simulateOnlinePayment(request))
                .build();
    }

    /**
     * Tạo đơn COD
     * POST /payments/cod/{orderId}
     */
    @PostMapping("/cod/{orderId}")
    public ApiResponse<PaymentResponse> createCOD(@PathVariable String orderId) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createCODPayment(orderId))
                .build();
    }

    /**
     * Admin xác nhận đã nhận tiền COD
     * POST /payments/cod/confirm/{orderId}
     */
    @PostMapping("/cod/confirm/{orderId}")
    public ApiResponse<PaymentResponse> confirmCOD(@PathVariable String orderId) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.confirmCODPayment(orderId))
                .build();
    }

    /**
     * Tạo link thanh toán PayOS
     * POST /payments/payos/create
     */
    @PostMapping("/payos/create")
    public ApiResponse<PayOSPaymentResponse> createPayOSPayment(@RequestBody PayOSPaymentRequest request) {
        return ApiResponse.<PayOSPaymentResponse>builder()
                .result(payOSService.createPaymentLink(request))
                .build();
    }

    /**
     * Tạo link thanh toán PayOS cho test (không cần order thật)
     * POST /payments/payos/test
     */
    @PostMapping("/payos/test")
    public ApiResponse<PayOSPaymentResponse> createPayOSTestPayment(@RequestBody PayOSTestRequest request) {
        return ApiResponse.<PayOSPaymentResponse>builder()
                .result(payOSService.createTestPaymentLink(request))
                .build();
    }

    /**
     * Webhook nhận kết quả thanh toán từ PayOS
     * POST /payments/payos/webhook
     */
    @PostMapping("/payos/webhook")
    public ApiResponse<Map<String, String>> payOSWebhook(@RequestBody Map<String, Object> webhookData) {
        payOSService.confirmPayment(webhookData);
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("status", "success"))
                .build();
    }
}