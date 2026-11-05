package com.example.fashion_ai.client;

import java.math.BigDecimal;

import com.example.fashion_ai.dto.ProductPageResponse;
import com.example.fashion_ai.dto.StoreProduct;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductApiClient {

    private final RestClient restClient;

    public ProductApiClient() {

        this.restClient = RestClient.builder()
                .baseUrl("https://farm2feast.onrender.com")
                .build();
    }

    public ProductPageResponse searchProducts(
            String jwtToken,
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long cursor,
            int limit) {

        return restClient.get()
                .uri(uriBuilder -> {

                    uriBuilder
                            .path("/api/user/products")
                            .queryParam("limit", limit);

                    if (keyword != null && !keyword.isBlank()) {
                        uriBuilder.queryParam("keyword", keyword);
                    }

                    if (category != null && !category.isBlank()) {
                        uriBuilder.queryParam("category", category);
                    }

                    if (minPrice != null) {
                        uriBuilder.queryParam("minPrice", minPrice);
                    }

                    if (maxPrice != null) {
                        uriBuilder.queryParam("maxPrice", maxPrice);
                    }

                    if (cursor != null) {
                        uriBuilder.queryParam("cursor", cursor);
                    }

                    return uriBuilder.build();
                })
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ProductPageResponse.class);
    }

    public StoreProduct getProductById(String jwtToken, Long productId) {

        return restClient.get()
                .uri("/api/user/products/{id}", productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(StoreProduct.class);
    }
}