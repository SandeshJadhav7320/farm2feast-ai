
package com.example.fashion_ai.order.tool;

import com.example.fashion_ai.order.client.OrderApiClient;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

public class AdminOrderTools {

    private final OrderApiClient orderApiClient;

    private final String jwtToken;

    public AdminOrderTools(
            OrderApiClient orderApiClient,
            String jwtToken) {

        this.orderApiClient = orderApiClient;
        this.jwtToken = jwtToken;
    }

    // TOOL 1: Get all orders

    @Tool(description = """
            Get all customer orders.
            Use this tool when the admin asks to see,
            view, list, or check all orders.
            """)
    public List<Map<String, Object>> getAllOrders() {

        return orderApiClient.getAllOrders(jwtToken);
    }

    // TOOL 2: Get a specific order

    @Tool(description = """
            Get complete details of a specific order
            using its order ID.
            Use this when the admin asks about one order.
            """)
    public Map<String, Object> getOrderById(
            @ToolParam(description = "The order ID")
            Long orderId) {

        return orderApiClient.getOrderById(
                jwtToken,
                orderId
        );
    }

    // TOOL 3: Get total order count

    @Tool(description = """
            Get the total number of orders.
            Use this when the admin asks how many orders
            have been placed.
            """)
    public Map<String, Object> getOrderCount() {

        return orderApiClient.getOrderCount(jwtToken);
    }

    // TOOL 4: Get revenue summary

    @Tool(description = """
            Get the overall revenue summary,
            total orders, delivered orders,
            paid orders and pending payment orders.
            Use this when the admin asks about revenue
            or overall sales performance.
            """)
    public Map<String, Object> getRevenueSummary() {

        return orderApiClient.getRevenueSummary(jwtToken);
    }

    // TOOL 5: Get revenue trend

    @Tool(description = """
            Get daily revenue for a specified number
            of recent days.
            Use this when the admin asks for a revenue
            trend or daily sales report.
            """)
    public List<Map<String, Object>> getRevenueTrend(
            @ToolParam(
                    description = "Number of days, between 1 and 365"
            )
            int days) {

        if (days < 1 || days > 365) {
            throw new IllegalArgumentException(
                    "Days must be between 1 and 365"
            );
        }

        return orderApiClient.getRevenueTrend(
                jwtToken,
                days
        );
    }
}
