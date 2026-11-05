package com.example.fashion_ai.dto;

public record AddToCartRequestDto(
        Long productId,
        Integer quantity
) {
}