package com.example.backend.dto.request;

import lombok.Data;

@Data
public class PayOSTestRequest {
    private String description;
    private Integer amount; // Số tiền trực tiếp (VND)
    private String returnUrl;
    private String cancelUrl;
}
