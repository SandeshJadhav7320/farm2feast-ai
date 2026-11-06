package com.example.fashion_ai.dto;

import java.math.BigDecimal;

public record ProductCardDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Integer quantity,
        String category
) {
}