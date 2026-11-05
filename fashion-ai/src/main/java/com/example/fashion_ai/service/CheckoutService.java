package com.example.fashion_ai.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.fashion_ai.client.CartApiClient;
import com.example.fashion_ai.client.OrderApiClient;
import com.example.fashion_ai.dto.CartItemResponseDto;
import com.example.fashion_ai.dto.checkout.CheckoutItemDto;
import com.example.fashion_ai.dto.checkout.CheckoutOrderRequestDto;
import com.example.fashion_ai.dto.order.CreateOrderRequestDto;
import com.example.fashion_ai.dto.order.OrderItemRequestDto;
import com.example.fashion_ai.dto.OrderResponseDto;
import com.example.fashion_ai.entity.PendingCheckout;
import com.example.fashion_ai.repository.PendingCheckoutRepository;

@Service
public class CheckoutService {

    private final CartApiClient cartApiClient;
    private final OrderApiClient orderApiClient;
    private final PendingCheckoutRepository repository;
    private final ObjectMapper objectMapper;

    public CheckoutService(
            CartApiClient cartApiClient,
            OrderApiClient orderApiClient,
            PendingCheckoutRepository repository,
            ObjectMapper objectMapper
    ) {
        this.cartApiClient = cartApiClient;
        this.orderApiClient = orderApiClient;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public boolean hasPendingCheckout(
            Long customerId,
            String conversationId
    ) {
        return repository
                .findFirstByCustomerIdAndConversationIdAndStatusOrderByCreatedAtDesc(
                        customerId,
                        conversationId,
                        "PENDING"
                )
                .filter(checkout ->
                        checkout.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    public String prepareCheckout(
            Long customerId,
            String conversationId,
            String jwtToken,
            String deliveryName,
            String phoneNumber,
            String deliveryAddress,
            String paymentMethod
    ) {

        if (deliveryName == null || deliveryName.isBlank()
                || phoneNumber == null || phoneNumber.isBlank()
                || deliveryAddress == null || deliveryAddress.isBlank()
                || paymentMethod == null || paymentMethod.isBlank()) {

            return "Please provide your name, phone number, delivery address and payment method.";
        }

        String method = paymentMethod.trim().toUpperCase();

        if (!method.equals("COD") && !method.equals("ONLINE")) {
            return "Payment method must be COD or ONLINE.";
        }

        if (!phoneNumber.trim().matches("^[+]?[0-9()\\s-]{7,20}$")) {
            return "Please provide a valid phone number.";
        }

        if (deliveryName.trim().length() > 100
                || deliveryAddress.trim().length() > 255) {
            return "Please check your delivery name or address length.";
        }

        try {
            List<CartItemResponseDto> cart =
                    cartApiClient.getMyCart(jwtToken);

            if (cart == null || cart.isEmpty()) {
                return "Your cart is empty. Please add products before checkout.";
            }

            List<CheckoutItemDto> items = cart.stream()
                    .map(item -> new CheckoutItemDto(
                            item.productId(),
                            item.quantity()
                    ))
                    .toList();

            CheckoutOrderRequestDto snapshot =
                    new CheckoutOrderRequestDto(
                            deliveryName.trim(),
                            phoneNumber.trim(),
                            deliveryAddress.trim(),
                            method,
                            items
                    );

            String itemsJson =
                    objectMapper.writeValueAsString(items);

            PendingCheckout checkout =
                    repository
                            .findFirstByCustomerIdAndConversationIdAndStatusOrderByCreatedAtDesc(
                                    customerId,
                                    conversationId,
                                    "PENDING"
                            )
                            .orElseGet(PendingCheckout::new);

            checkout.setCustomerId(customerId);
            checkout.setConversationId(conversationId);
            checkout.setDeliveryName(snapshot.deliveryName());
            checkout.setPhoneNumber(snapshot.phoneNumber());
            checkout.setDeliveryAddress(snapshot.deliveryAddress());
            checkout.setPaymentMethod(snapshot.paymentMethod());
            checkout.setItemsJson(itemsJson);
            checkout.setStatus("PENDING");
            checkout.setCreatedOrderId(null);
            checkout.setCreatedAt(LocalDateTime.now());
            checkout.setExpiresAt(LocalDateTime.now().plusMinutes(10));

            repository.save(checkout);

            StringBuilder summary = new StringBuilder();

            summary.append("Your checkout is ready.\n");
            summary.append("Delivery name: ")
                    .append(snapshot.deliveryName()).append("\n");
            summary.append("Phone: ")
                    .append(snapshot.phoneNumber()).append("\n");
            summary.append("Address: ")
                    .append(snapshot.deliveryAddress()).append("\n");
            summary.append("Payment method: ")
                    .append(snapshot.paymentMethod()).append("\n\n");

            summary.append("Items:\n");

            for (CartItemResponseDto item : cart) {
                summary.append(item.productName())
                        .append(" - Quantity: ")
                        .append(item.quantity())
                        .append("\n");
            }

            summary.append("\n");
            summary.append("Please review your order. ");
            summary.append("Type CONFIRM ORDER to place it. ");
            summary.append("This confirmation expires in 10 minutes.");

            return summary.toString();

        } catch (JsonProcessingException e) {
            return "Unable to prepare checkout. Please try again.";
        } catch (RestClientResponseException e) {
            return "Unable to retrieve your cart from the store. Please try again.";
        } catch (Exception e) {
            return "Unable to prepare checkout right now.";
        }
    }

    public String confirmCheckout(
            Long customerId,
            String conversationId,
            String jwtToken
    ) {

        PendingCheckout checkout =
                repository
                        .findFirstByCustomerIdAndConversationIdAndStatusOrderByCreatedAtDesc(
                                customerId,
                                conversationId,
                                "PENDING"
                        )
                        .orElse(null);

        if (checkout == null) {
            return "There is no pending checkout to confirm.";
        }

        if (!checkout.getExpiresAt().isAfter(LocalDateTime.now())) {
            checkout.setStatus("EXPIRED");
            repository.save(checkout);

            return "Your checkout has expired. Please prepare checkout again.";
        }

        try {
            List<CheckoutItemDto> items =
                    objectMapper.readValue(
                            checkout.getItemsJson(),
                            new TypeReference<List<CheckoutItemDto>>() {}
                    );

            List<OrderItemRequestDto> orderItems =
                    items.stream()
                            .map(item -> new OrderItemRequestDto(
                                    item.productId(),
                                    item.quantity()
                            ))
                            .toList();

            CreateOrderRequestDto request =
                    new CreateOrderRequestDto(
                            checkout.getDeliveryName(),
                            checkout.getPhoneNumber(),
                            checkout.getDeliveryAddress(),
                            checkout.getPaymentMethod(),
                            orderItems
                    );

            OrderResponseDto response =
                    orderApiClient.createOrder(jwtToken, request);

            if (response == null || response.id() == null) {
                return "The store did not return a valid order confirmation. Please check your orders before trying again.";
            }

            checkout.setStatus("COMPLETED");
            checkout.setCreatedOrderId(response.id());

            repository.save(checkout);

            return """
                    Your order was successfully created!

                    Order ID: %s
                    Total amount: ₹%s
                    Order status: %s
                    Payment method: %s
                    Payment status: %s
                    """.formatted(
                    response.id(),
                    response.totalAmount(),
                    response.status(),
                    response.paymentMethod(),
                    response.paymentStatus()
            );

        } catch (RestClientResponseException e) {
            return "The store rejected the order or could not process it. Please check your cart and try checkout again.";
        } catch (Exception e) {
            return "Unable to complete your checkout right now. Please check your order history before trying again.";
        }
    }
}