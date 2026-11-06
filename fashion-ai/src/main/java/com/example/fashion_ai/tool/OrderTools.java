
package com.example.fashion_ai.tool;

import com.example.fashion_ai.client.OrderApiClient;
import com.example.fashion_ai.dto.OrderItemResponseDto;
import com.example.fashion_ai.dto.OrderResponseDto;
import com.example.fashion_ai.service.ProductCollector;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

public class OrderTools {

    private final OrderApiClient orderApiClient;
    private final String jwtToken;
    private final ProductCollector productCollector;

    public OrderTools(
            OrderApiClient orderApiClient,
            String jwtToken,
            ProductCollector productCollector
    ) {
        this.orderApiClient = orderApiClient;
        this.jwtToken = jwtToken;
        this.productCollector = productCollector;
    }

    @Tool(description = """
            Get the authenticated customer's own orders.
            Use this tool when the customer asks about their orders,
            recent orders, or latest order.
            Do not ask for or provide a customer ID.
            """)
    public String getMyOrders() {

        try {
            List<OrderResponseDto> orders =
                    orderApiClient.getMyOrders(jwtToken);

            if (orders == null || orders.isEmpty()) {
                return "You do not have any orders yet.";
            }

            StringBuilder result = new StringBuilder();

            for (OrderResponseDto order : orders) {

                collectOrderProducts(order);

                result.append(formatOrder(order));
                result.append("\n--------------------\n");
            }

            return result.toString();

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 401
                    || e.getStatusCode().value() == 403) {
                return "You are not authorized to view these orders.";
            }

            return "Unable to retrieve your orders right now.";

        } catch (Exception e) {

            e.printStackTrace();
            return "An unexpected error occurred while retrieving your orders.";
        }
    }

    @Tool(description = """
            Get details and current status of one order belonging
            to the authenticated customer.
            Use this tool when the customer asks about a specific
            order ID. Never use an order ID as proof of ownership;
            the store backend verifies ownership.
            """)
    public String getMyOrderById(
            @ToolParam(description = "The order ID supplied by the customer")
            Long orderId
    ) {

        if (orderId == null || orderId <= 0) {
            return "Please provide a valid order ID.";
        }

        try {
            OrderResponseDto order =
                    orderApiClient.getMyOrderById(jwtToken, orderId);

            if (order == null) {
                return "Order not found.";
            }

            collectOrderProducts(order);

            return formatOrder(order);

        } catch (HttpClientErrorException.NotFound e) {

            return "Order not found.";

        } catch (HttpClientErrorException.Forbidden e) {

            return "You are not allowed to view this order.";

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 401) {
                return "Your session is not authorized. Please log in again.";
            }

            if (e.getStatusCode().value() == 403) {
                return "You are not allowed to view this order.";
            }

            return "Unable to retrieve this order right now.";

        } catch (Exception e) {

            e.printStackTrace();
            return "An unexpected error occurred while retrieving this order.";
        }
    }

    private void collectOrderProducts(OrderResponseDto order) {

        if (order == null || order.items() == null) {
            return;
        }

        for (OrderItemResponseDto item : order.items()) {
            productCollector.add(item);
        }
    }

    private String formatOrder(OrderResponseDto order) {

        StringBuilder result = new StringBuilder();

        result.append("Order ID: ").append(order.id()).append("\n");
        result.append("Order Date: ").append(order.createdAt()).append("\n");
        result.append("Total Amount: ₹").append(order.totalAmount()).append("\n");
        result.append("Order Status: ").append(order.status()).append("\n");
        result.append("Payment Method: ").append(order.paymentMethod()).append("\n");
        result.append("Payment Status: ").append(order.paymentStatus()).append("\n");

        if (order.deliveryCompany() != null) {
            result.append("Delivery Company: ")
                    .append(order.deliveryCompany()).append("\n");
        }

        if (order.trackingNumber() != null) {
            result.append("Tracking Number: ")
                    .append(order.trackingNumber()).append("\n");
        }

        result.append("Ordered Products:\n");

        if (order.items() != null) {

            for (OrderItemResponseDto item : order.items()) {

                result.append("- ")
                        .append(item.productName())
                        .append(" | Quantity: ")
                        .append(item.quantity())
                        .append(" | Unit Price: ₹")
                        .append(item.unitPrice())
                        .append(" | Subtotal: ₹")
                        .append(item.subtotal())
                        .append("\n");
            }
        }

        return result.toString();
    }
}
