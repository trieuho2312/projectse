package com.example.backend.service;

import com.example.backend.configuration.PayOSConfig;
import com.example.backend.dto.request.PayOSPaymentRequest;
import com.example.backend.dto.request.PayOSTestRequest;
import com.example.backend.dto.response.PayOSPaymentResponse;
import com.example.backend.entity.Order;
import com.example.backend.entity.Payment;
import com.example.backend.enums.OrderStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PayOSService {

    PayOSConfig payOSConfig;
    OrderRepository orderRepository;
    PaymentRepository paymentRepository;
    RestTemplate restTemplate;
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Tạo link thanh toán PayOS
     */
    @Transactional
    public PayOSPaymentResponse createPaymentLink(PayOSPaymentRequest request) {
        var order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));

        // Tạo orderCode từ orderId (PayOS yêu cầu số nguyên)
        long orderCode = generateOrderCode(order.getId());

        // Yêu cầu: Nội dung chuyển khoản chỉ hiển thị mã (ví dụ: CSGX0SVDCO5).
        // PayOS tự động ghép "mã giao dịch + description" để tạo nội dung chuyển khoản.
        // Để nội dung chỉ có mã, ta đặt description rỗng (luôn rỗng, bất kể request gửi gì).
        String description = "";

        // Tạo request body
        // PayOS yêu cầu amount và orderCode phải là integer
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("orderCode", (int) orderCode); // PayOS yêu cầu integer
        paymentData.put("amount", (int) order.getTotalAmount()); // PayOS yêu cầu integer
        paymentData.put("description", description);
        paymentData.put("returnUrl", request.getReturnUrl());
        paymentData.put("cancelUrl", request.getCancelUrl());

        // Tạo signature (PayOS yêu cầu field name là "signature")
        String signature = createChecksum(paymentData);

        Map<String, Object> requestBody = new HashMap<>(paymentData);
        requestBody.put("signature", signature);

        // Gọi API PayOS
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", payOSConfig.getClientId());
        headers.set("x-api-key", payOSConfig.getApiKey());
        
        // Log request để debug
        log.info("PayOS Request URL: {}", payOSConfig.getBaseUrl() + "/v2/payment-requests");
        log.info("PayOS Request Headers - x-client-id: {}, x-api-key: {}", 
                payOSConfig.getClientId(), 
                payOSConfig.getApiKey() != null ? "***" + payOSConfig.getApiKey().substring(Math.max(0, payOSConfig.getApiKey().length() - 4)) : "null");
        log.debug("PayOS Request Body: {}", requestBody);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String url = payOSConfig.getBaseUrl() + "/v2/payment-requests";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            log.info("PayOS API Response - Status: {}, Body: {}", response.getStatusCode(), responseBody);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                // Kiểm tra xem có field "code" và "desc" không (PayOS response format)
                if (jsonNode.has("code")) {
                    String code = jsonNode.get("code").asText();
                    String desc = jsonNode.has("desc") ? jsonNode.get("desc").asText() : "Unknown";
                    
                    if ("00".equals(code) && jsonNode.has("data")) {
                        JsonNode data = jsonNode.get("data");
                        if (data.has("checkoutUrl")) {
                            String checkoutUrl = data.get("checkoutUrl").asText();
                            String qrCode = data.has("qrCode") ? data.get("qrCode").asText() : null;

                            // Lưu payment với status PENDING
                            Payment payment = Payment.builder()
                                    .order(order)
                                    .paymentMethod("PAYOS")
                                    .paymentStatus("PENDING")
                                    .amount(order.getTotalAmount())
                                    .transactionId(String.valueOf(orderCode))
                                    .paymentDate(LocalDateTime.now())
                                    .build();

                            paymentRepository.save(payment);

                            log.info("PayOS payment link created for order {}: {}", order.getId(), checkoutUrl);

                            return PayOSPaymentResponse.builder()
                                    .checkoutUrl(checkoutUrl)
                                    .orderCode(String.valueOf(orderCode))
                                    .message("Tạo link thanh toán thành công")
                                    .qrCode(qrCode)
                                    .build();
                        } else {
                            log.error("PayOS response missing checkoutUrl in data: {}", responseBody);
                            throw new AppException(ErrorCode.PAYMENT_FAILED);
                        }
                    } else {
                        log.error("PayOS API returned error - Code: {}, Desc: {}, Full response: {}", code, desc, responseBody);
                        throw new AppException(ErrorCode.PAYMENT_FAILED);
                    }
                } else if (jsonNode.has("data") && jsonNode.get("data").has("checkoutUrl")) {
                    // Fallback: nếu response không có "code" nhưng có "data.checkoutUrl"
                    JsonNode data = jsonNode.get("data");
                    String checkoutUrl = data.get("checkoutUrl").asText();
                    String qrCode = data.has("qrCode") ? data.get("qrCode").asText() : null;
                    
                    // Lưu payment với status PENDING
                    Payment payment = Payment.builder()
                            .order(order)
                            .paymentMethod("PAYOS")
                            .paymentStatus("PENDING")
                            .amount(order.getTotalAmount())
                            .transactionId(String.valueOf(orderCode))
                            .paymentDate(LocalDateTime.now())
                            .build();

                    paymentRepository.save(payment);
                    
                    log.info("PayOS payment link created (fallback) for order {}: {}", order.getId(), checkoutUrl);
                    return PayOSPaymentResponse.builder()
                            .checkoutUrl(checkoutUrl)
                            .orderCode(String.valueOf(orderCode))
                            .message("Tạo link thanh toán thành công")
                            .qrCode(qrCode)
                            .build();
                } else {
                    log.error("PayOS response format unexpected: {}", responseBody);
                    throw new AppException(ErrorCode.PAYMENT_FAILED);
                }
            } else {
                log.error("PayOS API error - Status: {}, Body: {}", response.getStatusCode(), responseBody);
                throw new AppException(ErrorCode.PAYMENT_FAILED);
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorResponse = e.getResponseBodyAsString();
            log.error("PayOS HTTP Client Error - Status: {}, Response: {}", e.getStatusCode(), errorResponse);
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            String errorResponse = e.getResponseBodyAsString();
            log.error("PayOS HTTP Server Error - Status: {}, Response: {}", e.getStatusCode(), errorResponse);
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        } catch (Exception e) {
            log.error("Error creating PayOS payment link: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * Tạo link thanh toán PayOS cho test (không cần order thật)
     */
    public PayOSPaymentResponse createTestPaymentLink(PayOSTestRequest request) {
        // Tạo orderCode ngẫu nhiên cho test (thêm random để tránh trùng)
        long orderCode = (System.currentTimeMillis() % 900000000L + 100000000L) + new Random().nextInt(1000);

        // Yêu cầu: Nội dung chuyển khoản chỉ hiển thị mã (ví dụ: CSGX0SVDCO5).
        // PayOS tự động ghép "mã giao dịch + description" để tạo nội dung chuyển khoản.
        // Để nội dung chỉ có mã, ta đặt description rỗng (luôn rỗng, bất kể request gửi gì).
        String description = "";

        // Tạo request body
        // PayOS yêu cầu amount và orderCode phải là integer
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("orderCode", (int) orderCode); // PayOS yêu cầu integer
        paymentData.put("amount", request.getAmount() != null ? request.getAmount() : 0); // PayOS yêu cầu integer
        paymentData.put("description", description);
        paymentData.put("returnUrl", request.getReturnUrl());
        paymentData.put("cancelUrl", request.getCancelUrl());

        // Tạo signature (PayOS yêu cầu field name là "signature")
        String signature = createChecksum(paymentData);

        Map<String, Object> requestBody = new HashMap<>(paymentData);
        requestBody.put("signature", signature);

        // Gọi API PayOS
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", payOSConfig.getClientId());
        headers.set("x-api-key", payOSConfig.getApiKey());
        
        // Log request để debug
        log.info("PayOS Test Request URL: {}", payOSConfig.getBaseUrl() + "/v2/payment-requests");
        log.info("PayOS Request Headers - x-client-id: {}, x-api-key: {}", 
                payOSConfig.getClientId(), 
                payOSConfig.getApiKey() != null ? "***" + payOSConfig.getApiKey().substring(Math.max(0, payOSConfig.getApiKey().length() - 4)) : "null");
        log.debug("PayOS Request Body: {}", requestBody);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String url = payOSConfig.getBaseUrl() + "/v2/payment-requests";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            log.info("PayOS API Response - Status: {}, Body: {}", response.getStatusCode(), responseBody);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                // Kiểm tra xem có field "code" và "desc" không (PayOS response format)
                if (jsonNode.has("code")) {
                    String code = jsonNode.get("code").asText();
                    String desc = jsonNode.has("desc") ? jsonNode.get("desc").asText() : "Unknown";
                    
                    if ("00".equals(code) && jsonNode.has("data")) {
                        JsonNode data = jsonNode.get("data");
                        if (data.has("checkoutUrl")) {
                            String checkoutUrl = data.get("checkoutUrl").asText();
                            String qrCode = data.has("qrCode") ? data.get("qrCode").asText() : null;
                            log.info("PayOS test payment link created: {}", checkoutUrl);

                            return PayOSPaymentResponse.builder()
                                    .checkoutUrl(checkoutUrl)
                                    .orderCode(String.valueOf(orderCode))
                                    .message("Tạo link thanh toán test thành công")
                                    .qrCode(qrCode)
                                    .build();
                        } else {
                            log.error("PayOS response missing checkoutUrl in data: {}", responseBody);
                            throw new AppException(ErrorCode.PAYMENT_FAILED);
                        }
                    } else {
                        log.error("PayOS API returned error - Code: {}, Desc: {}, Full response: {}", code, desc, responseBody);
                        throw new AppException(ErrorCode.PAYMENT_FAILED);
                    }
                } else if (jsonNode.has("data") && jsonNode.get("data").has("checkoutUrl")) {
                    // Fallback: nếu response không có "code" nhưng có "data.checkoutUrl"
                    JsonNode data = jsonNode.get("data");
                    String checkoutUrl = data.get("checkoutUrl").asText();
                    String qrCode = data.has("qrCode") ? data.get("qrCode").asText() : null;
                    log.info("PayOS test payment link created (fallback): {}", checkoutUrl);
                    return PayOSPaymentResponse.builder()
                            .checkoutUrl(checkoutUrl)
                            .orderCode(String.valueOf(orderCode))
                            .message("Tạo link thanh toán test thành công")
                            .qrCode(qrCode)
                            .build();
                } else {
                    log.error("PayOS response format unexpected: {}", responseBody);
                    throw new AppException(ErrorCode.PAYMENT_FAILED);
                }
            } else {
                log.error("PayOS API error - Status: {}, Body: {}", response.getStatusCode(), responseBody);
                throw new AppException(ErrorCode.PAYMENT_FAILED);
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorResponse = e.getResponseBodyAsString();
            log.error("PayOS HTTP Client Error - Status: {}, Response: {}", e.getStatusCode(), errorResponse);
            try {
                log.error("Request that failed: {}", objectMapper.writeValueAsString(requestBody));
            } catch (Exception ex) {
                log.error("Request that failed: {}", requestBody);
            }
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            String errorResponse = e.getResponseBodyAsString();
            log.error("PayOS HTTP Server Error - Status: {}, Response: {}", e.getStatusCode(), errorResponse);
            try {
                log.error("Request that failed: {}", objectMapper.writeValueAsString(requestBody));
            } catch (Exception ex) {
                log.error("Request that failed: {}", requestBody);
            }
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        } catch (Exception e) {
            log.error("Error creating PayOS test payment link: {}", e.getMessage(), e);
            log.error("Request that failed: {}", requestBody);
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * Xác nhận thanh toán từ webhook PayOS
     */
    @Transactional
    public void confirmPayment(Map<String, Object> webhookData) {
        try {
            String code = webhookData.get("code").toString();
            String desc = webhookData.get("desc").toString();
            JsonNode data = objectMapper.valueToTree(webhookData.get("data"));

            if ("00".equals(code)) {
                long orderCode = data.get("orderCode").asLong();
                String transactionId = String.valueOf(orderCode);

                Payment payment = paymentRepository.findByTransactionId(transactionId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));

                payment.setPaymentStatus("SUCCESS");
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);

                Order order = payment.getOrder();
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                log.info("PayOS payment confirmed for order: {}", order.getId());
            } else {
                log.warn("PayOS payment failed: {}", desc);
            }
        } catch (Exception e) {
            log.error("Error confirming PayOS payment", e);
            throw new RuntimeException("Error confirming payment", e);
        }
    }

    /**
     * Tạo signature cho PayOS
     * PayOS yêu cầu thứ tự các field theo alphabet: amount, cancelUrl, description, orderCode, returnUrl
     * Theo tài liệu: https://payos.vn/docs/api/
     */
    private String createChecksum(Map<String, Object> data) {
        try {
            // PayOS yêu cầu thứ tự alphabet: amount, cancelUrl, description, orderCode, returnUrl
            // Format: amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
            // PayOS yêu cầu amount và orderCode là integer (không có dấu thập phân)
            Integer amount = ((Number) data.get("amount")).intValue();
            String cancelUrl = String.valueOf(data.get("cancelUrl"));
            String description = String.valueOf(data.get("description"));
            Integer orderCode = ((Number) data.get("orderCode")).intValue();
            String returnUrl = String.valueOf(data.get("returnUrl"));
            
            // PayOS yêu cầu signature được tính từ data được sort theo alphabet
            // Format: amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
            // Thử không URL encode trước (raw values)
            String dataStr = String.format(
                    "amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                    amount,
                    cancelUrl,
                    description,
                    orderCode,
                    returnUrl
            );
            
            log.info("PayOS Signature data string: {}", dataStr);
            
            // Kiểm tra và clean checksum key
            String checksumKey = payOSConfig.getChecksumKey();
            if (checksumKey == null || checksumKey.isEmpty()) {
                log.error("PayOS Checksum Key is NULL or empty!");
                throw new RuntimeException("PayOS checksum key chưa được cấu hình");
            }
            
            // Loại bỏ space và newline từ checksum key
            checksumKey = checksumKey.trim().replaceAll("[\\r\\n]", "");
            log.info("PayOS Checksum Key length: {}", checksumKey.length());
            log.info("PayOS Checksum Key (last 8 chars): {}", checksumKey.length() >= 8 
                    ? "***" + checksumKey.substring(checksumKey.length() - 8) : "null");

            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    checksumKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmacSHA256.init(secretKey);
            byte[] hash = hmacSHA256.doFinal(dataStr.getBytes(StandardCharsets.UTF_8));
            
            // PayOS yêu cầu HEX signature (theo code mẫu hoạt động)
            String signature = bytesToHex(hash);
            
            log.info("Generated signature (hex): {}", signature);
            log.info("Signature length: {}", signature.length());
            
            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error creating signature", e);
            throw new RuntimeException("Error creating signature", e);
        }
    }
    
    /**
     * Convert byte array to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Tạo orderCode từ orderId (PayOS yêu cầu số nguyên)
     */
    private long generateOrderCode(String orderId) {
        // Sử dụng hash của orderId để tạo số nguyên
        return Math.abs(orderId.hashCode()) % 900000000L + 100000000L;
    }
}
