package com.example.fashion_ai.client;

import com.example.fashion_ai.dto.AddToCartRequestDto;
import com.example.fashion_ai.dto.CartItemResponseDto;
import com.example.fashion_ai.dto.UpdateCartRequestDto;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CartApiClient {

    private final RestClient restClient;

    public CartApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://farm2feast.onrender.com")
                .build();
    }

    public List<CartItemResponseDto> getMyCart(String jwtToken) {

        return restClient.get()
                .uri("/api/cart")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(
                        new ParameterizedTypeReference<List<CartItemResponseDto>>() {}
                );
    }

    public String addToCart(
            String jwtToken,
            Long productId,
            Integer quantity
    ) {

        AddToCartRequestDto request =
                new AddToCartRequestDto(productId, quantity);

        return restClient.post()
                .uri("/api/cart/add")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    public String updateCartItem(
        String jwtToken,
        Long cartItemId,
        Integer quantity
) {

    UpdateCartRequestDto request =
            new UpdateCartRequestDto(quantity);

    return restClient.put()
            .uri("/api/cart/update/{cartItemId}", cartItemId)
            .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + jwtToken
            )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_PLAIN)
            .body(request)
            .retrieve()
            .body(String.class);
}

public String removeCartItem(
        String jwtToken,
        Long cartItemId
) {

    return restClient.delete()
            .uri("/api/cart/remove/{cartItemId}", cartItemId)
            .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + jwtToken
            )
            .accept(MediaType.TEXT_PLAIN)
            .retrieve()
            .body(String.class);
}
}