package com.example.fashion_ai.dto;

import java.util.List;

public class ChatResponse {

    private String response;

    private List<ProductCardDto> products;

    public ChatResponse(String response) {
        this(response, List.of());
    }

    public ChatResponse(
            String response,
            List<ProductCardDto> products
    ) {
        this.response = response;
        this.products = products == null
                ? List.of()
                : List.copyOf(products);
    }

    public String getResponse() {
        return response;
    }

    public List<ProductCardDto> getProducts() {
        return products;
    }
}