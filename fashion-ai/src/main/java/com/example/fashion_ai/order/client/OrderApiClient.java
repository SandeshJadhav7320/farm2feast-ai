
package com.example.fashion_ai.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OrderApiClient {

    private final RestClient restClient;

    public OrderApiClient(
            @Value("${store.api.base-url}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // 1. Get all orders
    public List<Map<String, Object>> getAllOrders(
            String jwtToken) {

        return restClient.get()
                .uri("/api/admin/orders")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // 2. Get one order
    public Map<String, Object> getOrderById(
            String jwtToken,
            Long orderId) {

        return restClient.get()
                .uri("/api/admin/orders/{orderId}", orderId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // 3. Get total order count
    public Map<String, Object> getOrderCount(
            String jwtToken) {

        return restClient.get()
                .uri("/api/admin/orders/count")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // 4. Get revenue summary
    public Map<String, Object> getRevenueSummary(
            String jwtToken) {

        return restClient.get()
                .uri("/api/admin/orders/revenue-summary")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // 5. Get revenue trend
    public List<Map<String, Object>> getRevenueTrend(
            String jwtToken,
            int days) {

        return restClient.get()
                .uri(
                        "/api/admin/orders/revenue-trend?days={days}",
                        days
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
