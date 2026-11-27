
package com.example.fashion_ai.order.tool;

import com.example.fashion_ai.order.client.OrderApiClient;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

public class AdminOrderTools {

    private final OrderApiClient orderApiClient;
    private final String jwtToken;

    // Stores the structured result returned by the latest tool call.
    private Object lastResult;
    private String lastResultType;

    public AdminOrderTools(
            OrderApiClient orderApiClient,
            String jwtToken) {

        this.orderApiClient = orderApiClient;
        this.jwtToken = jwtToken;
    }

    // ==========================================
    // TOOL 1: GET ALL ORDERS
    // ==========================================

    @Tool(description = """
            Get all customer orders.
            Use this tool when the admin asks to see,
            view, list, or check all orders.
            """)
    public List<Map<String, Object>> getAllOrders() {

        List<Map<String, Object>> orders =
                orderApiClient.getAllOrders(jwtToken);

        this.lastResult = orders;
        this.lastResultType = "ALL_ORDERS";

        return orders;
    }

    // ==========================================
    // TOOL 2: GET ONE ORDER
    // ==========================================

    @Tool(description = """
            Get complete details of a specific order
            using its order ID.
            Use this when the admin asks about one order.
            """)
    public Map<String, Object> getOrderById(
            @ToolParam(description = "The order ID")
            Long orderId) {

        Map<String, Object> order =
                orderApiClient.getOrderById(
                        jwtToken,
                        orderId
                );

        this.lastResult = order;
        this.lastResultType = "SINGLE_ORDER";

        return order;
    }

    // ==========================================
    // TOOL 3: GET ORDER COUNT
    // ==========================================

    @Tool(description = """
            Get the total number of orders.
            Use this when the admin asks how many orders
            have been placed.
            """)
    public Map<String, Object> getOrderCount() {

        Map<String, Object> result =
                orderApiClient.getOrderCount(jwtToken);

        this.lastResult = result;
        this.lastResultType = "ORDER_COUNT";

        return result;
    }

    // ==========================================
    // TOOL 4: GET REVENUE SUMMARY
    // ==========================================

    @Tool(description = """
            Get the overall revenue summary,
            total orders, delivered orders,
            paid orders and pending payment orders.
            Use this when the admin asks about revenue
            or overall sales performance.
            """)
    public Map<String, Object> getRevenueSummary() {

        Map<String, Object> result =
                orderApiClient.getRevenueSummary(jwtToken);

        this.lastResult = result;
        this.lastResultType = "REVENUE_SUMMARY";

        return result;
    }

    // ==========================================
    // TOOL 5: GET REVENUE TREND
    // ==========================================

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

        List<Map<String, Object>> result =
                orderApiClient.getRevenueTrend(
                        jwtToken,
                        days
                );

        this.lastResult = result;
        this.lastResultType = "REVENUE_TREND";

        return result;
    }

    // ==========================================
    // GET STRUCTURED RESULT
    // These are Java methods, NOT AI tools.
    // ==========================================

    public Object getLastResult() {
        return lastResult;
    }

    public String getLastResultType() {
        return lastResultType;
    }
}
