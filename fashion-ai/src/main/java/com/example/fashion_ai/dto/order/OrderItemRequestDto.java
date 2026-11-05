package com.example.fashion_ai.dto.order;

public record OrderItemRequestDto(
        Long productId,
        Integer quantity
) {}