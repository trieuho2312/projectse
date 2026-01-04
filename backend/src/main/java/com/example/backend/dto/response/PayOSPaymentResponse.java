package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayOSPaymentResponse {
    private String checkoutUrl;
    private String orderCode;
    private String message;
    private String qrCode; // QR code để hiển thị popup
}
