package com.example.fashion_ai.client;

import com.example.fashion_ai.dto.OrderResponseDto;
import com.example.fashion_ai.dto.order.CreateOrderRequestDto;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class OrderApiClient {

    private final RestClient restClient;

    public OrderApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://farm2feast.onrender.com")
                .build();
    }

    public List<OrderResponseDto> getMyOrders(String jwtToken) {

        return restClient.get()
                .uri("/api/user/orders")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OrderResponseDto>>() {});
    }

    public OrderResponseDto getMyOrderById(
            String jwtToken,
            Long orderId
    ) {

        return restClient.get()
                .uri("/api/user/orders/{orderId}", orderId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OrderResponseDto.class);
    }

        public OrderResponseDto createOrder(
                String jwtToken,
                CreateOrderRequestDto request
        ) {
        return restClient.post()
                .uri("/api/user/orders")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OrderResponseDto.class);
        }

        
}