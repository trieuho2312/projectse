package com.example.backend.dto.request;

import lombok.Data;

@Data
public class PayOSPaymentRequest {
    private String orderId;
    private String description;
    private String returnUrl;
    private String cancelUrl;
}
