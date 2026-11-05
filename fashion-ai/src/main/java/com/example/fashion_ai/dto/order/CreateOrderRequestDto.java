package com.example.fashion_ai.dto.order;

import java.util.List;

public record CreateOrderRequestDto(
        String deliveryName,
        String phoneNumber,
        String deliveryAddress,
        String paymentMethod,
        List<OrderItemRequestDto> items
) {}