package com.example.fashion_ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponseDto(
        Long id,
        BigDecimal totalAmount,
        String status,
        String paymentMethod,
        String paymentStatus,
        LocalDateTime createdAt,
        String deliveryCompany,
        String trackingNumber,
        List<OrderItemResponseDto> items
) {
}