package com.example.fashion_ai.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.fashion_ai.dto.OrderItemResponseDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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