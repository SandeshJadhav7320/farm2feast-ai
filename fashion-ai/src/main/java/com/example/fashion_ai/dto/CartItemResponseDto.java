package com.example.fashion_ai.dto;

import java.math.BigDecimal;

public record CartItemResponseDto(
        Long cartItemId,
        Long productId,
        String productName,
        String description,
        BigDecimal price,
        String imageUrl,
        Integer quantity,
        BigDecimal totalPrice
) {
}