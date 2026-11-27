
package com.example.fashion_ai.assistant.service;

import com.example.fashion_ai.admin.service.AdminProductChatService;
import com.example.fashion_ai.admin.service.AdminProductUpdateChatService;

import com.example.fashion_ai.assistant.dto.AdminAssistantChatResponse;
import com.example.fashion_ai.assistant.dto.AdminAssistantIntent;

import com.example.fashion_ai.order.client.OrderApiClient;
import com.example.fashion_ai.order.tool.AdminOrderTools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminAssistantService {

    private final ChatClient chatClient;

    private final AdminProductChatService addProductService;
    private final AdminProductUpdateChatService updateProductService;

    private final OrderApiClient orderApiClient;

    // Remember the selected operation for each admin conversation.
    private final Map<String, String> conversationOperations =
            new ConcurrentHashMap<>();

    public AdminAssistantService(
            ChatClient.Builder builder,
            AdminProductChatService addProductService,
            AdminProductUpdateChatService updateProductService,
            OrderApiClient orderApiClient) {

        this.chatClient = builder.build();
        this.addProductService = addProductService;
        this.updateProductService = updateProductService;
        this.orderApiClient = orderApiClient;
    }

    public AdminAssistantChatResponse chat(
            String adminId,
            String conversationId,
            String jwtToken,
            String message) {

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Message cannot be empty."
            );
        }

        if (message.length() > 2000) {
            throw new IllegalArgumentException(
                    "Message is too long."
            );
        }

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        String key = adminId + ":" + conversationId;

        // Use the operation already selected for this conversation.
        String operation = conversationOperations.get(key);

        // For a new conversation, ask AI to identify the operation.
        if (operation == null) {
            operation = detectIntent(message);

            if ("UNKNOWN".equals(operation)) {
                return new AdminAssistantChatResponse(
                        conversationId,
                        "UNKNOWN",
                        "Would you like to add a product, update a product, or view orders and sales information?",
                        null,
                        false
                );
            }

            conversationOperations.put(key, operation);
        }

        // ADD PRODUCT
        if ("ADD_PRODUCT".equals(operation)) {

            var result = addProductService.chat(
                    adminId,
                    conversationId,
                    message
            );

            return new AdminAssistantChatResponse(
                    result.getConversationId(),
                    operation,
                    result.getReply(),
                    result.getDraft(),
                    result.isReadyForConfirmation()
            );
        }

        // UPDATE PRODUCT
        if ("UPDATE_PRODUCT".equals(operation)) {

            var result = updateProductService.chat(
                    adminId,
                    conversationId,
                    jwtToken,
                    message
            );

            return new AdminAssistantChatResponse(
                    result.getConversationId(),
                    operation,
                    result.getReply(),
                    result.getDraft(),
                    result.isReadyForConfirmation()
            );
        }


        // ==========================================
        // ORDER OPERATION
        if ("ORDER".equals(operation)) {

            AdminOrderTools orderTools =
                    new AdminOrderTools(orderApiClient, jwtToken);

            String reply = chatClient.prompt()
                    .system("""
                        You are Farm2Feast Admin Order Assistant.

                        Help the admin manage and retrieve customer orders.

                        You have these tools:

                        1. getAllOrders
                        Use when the admin asks to see all orders.

                        2. getOrderById
                        Use when the admin asks about a particular order.

                        3. getOrderCount
                        Use when the admin asks how many orders exist.

                        4. getRevenueSummary
                        Use for revenue summary questions.

                        5. getRevenueTrend
                        Use for revenue trend questions.

                        6. changeOrderStatus
                        Use when the admin explicitly asks to change an
                        order's status.

                        IMPORTANT RULES:

                        - Always use tools to retrieve or update real data.
                        - Never invent order information.
                        - For status updates, use the exact order ID provided.
                        - Convert status names to uppercase.
                        - Never claim an update succeeded unless the tool
                        successfully returns.
                        - If the order ID or new status is missing, ask the
                        admin for the missing information.
                        - Do not change an order status when the admin is
                        only asking what the status means.
                        - After a successful status update, clearly mention
                        the order ID and its new status.
                        - If the tool returns an error, explain the error
                        without claiming success.
                        """)
                    .user(message)
                    .tools(orderTools)
                    .call()
                    .content();

            return new AdminAssistantChatResponse(
                    conversationId,
                    "ORDER",
                    reply,
                    null,
                    false,
                    orderTools.getLastResultType(),
                    orderTools.getLastResult()
            );
        }


        throw new IllegalStateException(
                "Unsupported assistant operation: " + operation
        );
    }

    private String detectIntent(String message) {

        AdminAssistantIntent result = chatClient.prompt()
                .system("""
                        You are an intent classifier for the
                        Farm2Feast administrator assistant.

                        Classify the administrator's message into
                        exactly one of these intents:

                        ADD_PRODUCT:
                        The admin wants to create or add a new product.

                        UPDATE_PRODUCT:
                        The admin wants to edit or change an existing product.

                        ORDER:
                        The admin wants to view orders, one order,
                        order count, revenue summary or revenue trend.

                        UNKNOWN:
                        The request is unrelated or unclear.

                        Examples:

                        "Add a new rice product" -> ADD_PRODUCT

                        "Create a product named Apple" -> ADD_PRODUCT

                        "Change product 10 price to 200" -> UPDATE_PRODUCT

                        "Update product 15" -> UPDATE_PRODUCT

                        "Show all orders" -> ORDER

                        "Show order 25" -> ORDER

                        "How much revenue did we make?" -> ORDER

                        "Show revenue for 30 days" -> ORDER

                        Return only the intent field.
                        Do not execute any operation.
                        """)
                .user(message)
                .call()
                .entity(AdminAssistantIntent.class);

        if (result == null || result.getIntent() == null) {
            return "UNKNOWN";
        }

        String intent = result.getIntent()
                .trim()
                .toUpperCase();

        if (intent.equals("ADD_PRODUCT")
                || intent.equals("UPDATE_PRODUCT")
                || intent.equals("ORDER")) {
            return intent;
        }

        return "UNKNOWN";
    }

    public Object confirm(
            String adminId,
            String conversationId,
            String jwtToken,
            MultipartFile image) {

        String key = adminId + ":" + conversationId;

        String operation = conversationOperations.get(key);

        if (operation == null) {
            throw new IllegalArgumentException(
                    "Conversation not found or expired."
            );
        }

        // Add Product requires an image.
        if ("ADD_PRODUCT".equals(operation)) {

            var result = addProductService.confirm(
                    adminId,
                    conversationId,
                    jwtToken,
                    image
            );

            conversationOperations.remove(key);

            return result;
        }

        // Update Product allows an optional image.
        if ("UPDATE_PRODUCT".equals(operation)) {

            var result = updateProductService.confirm(
                    adminId,
                    conversationId,
                    jwtToken,
                    image
            );

            conversationOperations.remove(key);

            return result;
        }

        throw new IllegalArgumentException(
                "This operation does not support confirmation."
        );
    }

    public void cancel(
            String adminId,
            String conversationId) {

        String key = adminId + ":" + conversationId;

        String operation = conversationOperations.get(key);

        if (operation == null) {
            return;
        }

        if ("ADD_PRODUCT".equals(operation)) {
            addProductService.cancel(adminId, conversationId);
        }

        if ("UPDATE_PRODUCT".equals(operation)) {
            updateProductService.cancel(adminId, conversationId);
        }

        conversationOperations.remove(key);
    }
}
