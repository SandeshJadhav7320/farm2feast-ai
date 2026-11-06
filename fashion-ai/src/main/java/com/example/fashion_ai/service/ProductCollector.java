package com.example.fashion_ai.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.fashion_ai.dto.CartItemResponseDto;
import com.example.fashion_ai.dto.OrderItemResponseDto;
import com.example.fashion_ai.dto.ProductCardDto;
import com.example.fashion_ai.dto.StoreProduct;

public class ProductCollector {

    private final Map<Long, ProductCardDto> products =
            new LinkedHashMap<>();

    public void add(StoreProduct product) {

        if (product == null || product.getId() == null) {
            return;
        }

        products.put(
                product.getId(),
                new ProductCardDto(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImageUrl(),
                        product.getQuantity(),
                        product.getCategory()
                )
        );
    }

    public void add(CartItemResponseDto item) {

        if (item == null || item.productId() == null) {
            return;
        }

        products.put(
                item.productId(),
                new ProductCardDto(
                        item.productId(),
                        item.productName(),
                        item.description(),
                        item.price(),
                        item.imageUrl(),
                        item.quantity(),
                        null
                )
        );
    }

    public void add(OrderItemResponseDto item) {

        if (item == null || item.productId() == null) {
            return;
        }

        products.put(
                item.productId(),
                new ProductCardDto(
                        item.productId(),
                        item.productName(),
                        null,
                        item.unitPrice(),
                        item.imageUrl(),
                        item.quantity(),
                        null
                )
        );
    }

    public List<ProductCardDto> getProducts() {
        return new ArrayList<>(products.values());
    }
}