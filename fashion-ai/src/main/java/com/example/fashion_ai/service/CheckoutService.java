
package com.example.fashion_ai.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.fashion_ai.client.CartApiClient;
import com.example.fashion_ai.client.OrderProductApiClient;
import com.example.fashion_ai.client.ProductApiClient;
import com.example.fashion_ai.dto.CartItemResponseDto;
import com.example.fashion_ai.dto.checkout.CheckoutItemDto;
import com.example.fashion_ai.dto.checkout.CheckoutOrderRequestDto;
import com.example.fashion_ai.dto.order.CreateOrderRequestDto;
import com.example.fashion_ai.dto.order.OrderItemRequestDto;
import com.example.fashion_ai.dto.OrderResponseDto;
import com.example.fashion_ai.dto.StoreProduct;
import com.example.fashion_ai.entity.PendingCheckout;
import com.example.fashion_ai.repository.PendingCheckoutRepository;

@Service
public class CheckoutService {

    private final CartApiClient cartApiClient;
    private final OrderProductApiClient orderApiClient;
    private final PendingCheckoutRepository repository;
    private final ObjectMapper objectMapper;
    private final ProductApiClient productApiClient;

    public CheckoutService(
            CartApiClient cartApiClient,
            OrderProductApiClient orderApiClient,
            PendingCheckoutRepository repository,
            ObjectMapper objectMapper,
            ProductApiClient productApiClient
    ) {
        this.cartApiClient = cartApiClient;
        this.orderApiClient = orderApiClient;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.productApiClient = productApiClient;
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

    // Backward-compatible method.
    public String prepareCheckout(
            Long customerId,
            String conversationId,
            String jwtToken,
            String deliveryName,
            String phoneNumber,
            String deliveryAddress,
            String paymentMethod
    ) {
        return prepareCheckout(
                customerId,
                conversationId,
                jwtToken,
                deliveryName,
                phoneNumber,
                deliveryAddress,
                paymentMethod,
                null
        );
    }

    // New method that also collects cart products.
    public String prepareCheckout(
            Long customerId,
            String conversationId,
            String jwtToken,
            String deliveryName,
            String phoneNumber,
            String deliveryAddress,
            String paymentMethod,
            ProductCollector productCollector
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

            // Collect actual cart products for frontend product cards.
            if (productCollector != null) {
                for (CartItemResponseDto item : cart) {
                    productCollector.add(item);
                }
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

    // Backward-compatible method.
    public String confirmCheckout(
            Long customerId,
            String conversationId,
            String jwtToken
    ) {
        return confirmCheckout(
                customerId,
                conversationId,
                jwtToken,
                null
        );
    }

    // New method that also collects successfully ordered products.
    public String confirmCheckout(
            Long customerId,
            String conversationId,
            String jwtToken,
            ProductCollector productCollector
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

            // Collect products only after the store confirms order creation.
            if (productCollector != null
                    && response.items() != null) {

                response.items().forEach(productCollector::add);
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

    public String prepareDirectCheckout(
        Long customerId,
        String conversationId,
        String jwtToken,
        String deliveryName,
        String phoneNumber,
        String deliveryAddress,
        String paymentMethod,
        List<CheckoutItemDto> requestedItems,
        ProductCollector productCollector
) {

    try {

        // STEP 1: Validate delivery details

        if (deliveryName == null || deliveryName.isBlank()) {
            return "Please provide your delivery name.";
        }

        if (phoneNumber == null ||
                !phoneNumber.matches("^[0-9]{10}$")) {

            return "Please provide a valid 10-digit phone number.";
        }

        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            return "Please provide your complete delivery address.";
        }

        if (deliveryName.length() > 100 ||
                deliveryAddress.length() > 500) {

            return "Delivery name or address is too long.";
        }

        if (paymentMethod == null ||
                !(paymentMethod.equalsIgnoreCase("COD")
                        || paymentMethod.equalsIgnoreCase("ONLINE"))) {

            return "Please select COD or ONLINE payment.";
        }


        // STEP 2: Validate requested products

        if (requestedItems == null || requestedItems.isEmpty()) {
            return "Please tell me which products you want to order.";
        }

        Set<Long> productIds = new HashSet<>();

        List<CheckoutItemDto> validItems = new ArrayList<>();

        StringBuilder orderSummary = new StringBuilder();


        // STEP 3: Verify every product

        for (CheckoutItemDto item : requestedItems) {

            if (item == null ||
                    item.productId() == null ||
                    item.productId() <= 0 ||
                    item.quantity() == null ||
                    item.quantity() <= 0) {

                return "Invalid product ID or quantity.";
            }

            if (!productIds.add(item.productId())) {

                return "The same product was included more than once. Please provide each product only once.";
            }


            // Get actual product from store backend

            StoreProduct product = productApiClient.getProductById(
                    jwtToken,
                    item.productId()
            );

            if (product == null) {

                return "Product not found. Please select a valid product.";
            }


            // IMPORTANT:
            // Your StoreProduct class has getQuantity(),
            // NOT getAvailableQuantity().

            Integer availableQuantity = product.getQuantity();

            if (availableQuantity == null ||
                    availableQuantity < item.quantity()) {

                return "Sorry, "
                        + product.getName()
                        + " has only "
                        + availableQuantity
                        + " packets available.";
            }


            // Save the verified item

            validItems.add(
                    new CheckoutItemDto(
                            item.productId(),
                            item.quantity()
                    )
            );


            // Prepare order summary

            orderSummary
                    .append("- ")
                    .append(product.getName())
                    .append(" | Quantity: ")
                    .append(item.quantity())
                    .append("\n");
        }


        // STEP 4: Convert products into JSON

        String itemsJson = objectMapper.writeValueAsString(validItems);


        // STEP 5: Find existing pending checkout

        // IMPORTANT:
        // Use the same repository finder method
        // that your existing prepareCheckout() method uses.

        PendingCheckout pendingCheckout =
                repository
                        .findFirstByCustomerIdAndConversationIdAndStatusOrderByCreatedAtDesc(
                                customerId,
                                conversationId,
                                "PENDING"
                        )
                        .orElse(null);


        // STEP 6: Create pending checkout if required

        if (pendingCheckout == null) {

            pendingCheckout = new PendingCheckout();
        }


        // STEP 7: Save checkout information

        pendingCheckout.setCustomerId(customerId);

        pendingCheckout.setConversationId(conversationId);

        pendingCheckout.setDeliveryName(deliveryName);

        pendingCheckout.setPhoneNumber(phoneNumber);

        pendingCheckout.setDeliveryAddress(deliveryAddress);

        pendingCheckout.setPaymentMethod(paymentMethod.toUpperCase());

        pendingCheckout.setItemsJson(itemsJson);

        pendingCheckout.setStatus("PENDING");

        pendingCheckout.setCreatedOrderId(null);

        pendingCheckout.setCreatedAt(
                java.time.LocalDateTime.now()
        );

        pendingCheckout.setExpiresAt(
                java.time.LocalDateTime.now().plusMinutes(10)
        );


        repository.save(pendingCheckout);


        // STEP 8: Return checkout summary

        return """
                Your direct order is ready for confirmation!

                Products:
                %s

                Delivery Name: %s
                Phone Number: %s
                Delivery Address: %s
                Payment Method: %s

                Your checkout will expire in 10 minutes.

                Please review your order details.

                Type CONFIRM ORDER to place your order.
                """.formatted(
                orderSummary,
                deliveryName,
                phoneNumber,
                deliveryAddress,
                paymentMethod.toUpperCase()
        );


    } catch (org.springframework.web.client.RestClientResponseException e) {

        return "Unable to verify products with the store. Please try again.";

    } catch (Exception e) {

        e.printStackTrace();

        return "Something went wrong while preparing your direct order.";
    }
}
}
