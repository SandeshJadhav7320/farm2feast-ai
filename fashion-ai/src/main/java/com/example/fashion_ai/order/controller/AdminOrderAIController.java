
package com.example.fashion_ai.order.controller;

import com.example.fashion_ai.order.client.OrderApiClient;
import com.example.fashion_ai.order.dto.OrderAIChatRequest;
import com.example.fashion_ai.order.dto.OrderAIChatResponse;
import com.example.fashion_ai.order.tool.AdminOrderTools;

import jakarta.validation.Valid;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-ai/orders")
public class AdminOrderAIController {

    private final ChatClient chatClient;

    private final OrderApiClient orderApiClient;

    public AdminOrderAIController(
            ChatClient.Builder chatClientBuilder,
            OrderApiClient orderApiClient) {

        this.chatClient = chatClientBuilder.build();
        this.orderApiClient = orderApiClient;
    }

    @PostMapping("/chat")
    public ResponseEntity<OrderAIChatResponse> chat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrderAIChatRequest request) {

        // Check that the current user is an admin.
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        boolean isAdmin = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException(
                    "Only administrators can use order AI tools"
            );
        }

        // Create tools using the current admin's JWT.
        AdminOrderTools orderTools =
                new AdminOrderTools(
                        orderApiClient,
                        jwt.getTokenValue()
                );

        // Send the message to AI and allow tool calling.
        String reply = chatClient.prompt()
                .system("""
                        You are the Farm2Feast admin order assistant.

                        Help administrators view orders and
                        understand order and revenue information.

                        Use the available tools whenever the
                        admin requests actual order information.

                        Never invent order IDs, customer details,
                        payment information, order statuses,
                        or revenue figures.

                        If the admin asks about a specific order,
                        use the order ID provided by the admin.

                        If required information is missing,
                        ask a clarification question.

                        Only provide information returned by tools.
                        """)
                .user(request.getMessage())
                .tools(orderTools)
                .call()
                .content();

        return ResponseEntity.ok(
                new OrderAIChatResponse(reply)
        );
    }
}
