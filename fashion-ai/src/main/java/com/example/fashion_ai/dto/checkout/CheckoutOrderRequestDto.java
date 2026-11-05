package com.example.fashion_ai.dto.checkout;

import java.util.List;

public record CheckoutOrderRequestDto(
        String deliveryName,
        String phoneNumber,
        String deliveryAddress,
        String paymentMethod,
        List<CheckoutItemDto> items
) {}