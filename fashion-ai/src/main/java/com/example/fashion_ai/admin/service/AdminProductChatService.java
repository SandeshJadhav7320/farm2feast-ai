
package com.example.fashion_ai.admin.service;

import com.example.fashion_ai.admin.dto.AdminProductDraft;
import com.example.fashion_ai.admin.dto.AdminProductExtraction;
import com.example.fashion_ai.admin.dto.AdminProductChatResponse;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminProductChatService {

    private final ChatClient chatClient;

    private final AdminProductService adminProductService;

    // Temporary conversation storage.
    // The key contains both admin ID and conversation ID.
    private final Map<String, AdminProductDraft> drafts =
            new ConcurrentHashMap<>();

    public AdminProductChatService(
            ChatClient.Builder chatClientBuilder,
            AdminProductService adminProductService) {

        this.chatClient = chatClientBuilder.build();
        this.adminProductService = adminProductService;
    }

    public AdminProductChatResponse chat(
            String adminId,
            String conversationId,
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

        // Create a conversation ID for a new conversation.
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        String key = adminId + ":" + conversationId;

        AdminProductDraft draft = drafts.computeIfAbsent(
                key,
                k -> new AdminProductDraft()
        );

        // Ask AI to extract only information explicitly
        // provided in the latest message.
        String prompt = """
                You are an assistant helping a store administrator
                prepare a new Makhana product.

                Extract product information from the administrator's
                latest message.

                Existing product details:
                Name: %s
                Description: %s
                Price: %s
                Quantity: %s
                Category: %s

                Latest administrator message:
                %s

                Rules:
                1. Extract only details explicitly supplied by the admin.
                2. Do not invent missing information.
                3. If the message does not provide a field, return null
                   for that field.
                4. Price must be a positive number.
                5. Quantity must be zero or greater.
                6. Do not create, update, or delete any product.
                7. Return only the extracted product fields.
                """.formatted(
                value(draft.getName()),
                value(draft.getDescription()),
                value(draft.getPrice()),
                value(draft.getQuantity()),
                value(draft.getCategory()),
                message
        );

        AdminProductExtraction extracted = chatClient
                .prompt()
                .user(prompt)
                .call()
                .entity(AdminProductExtraction.class);

        if (extracted == null) {
            throw new IllegalStateException(
                    "AI could not understand the product details."
            );
        }

        // Update only the fields extracted from this message.
        mergeDraft(draft, extracted);

        String reply = buildNextQuestion(draft);

        return new AdminProductChatResponse(
                conversationId,
                reply,
                draft,
                draft.isComplete()
        );
    }

    private void mergeDraft(
            AdminProductDraft draft,
            AdminProductExtraction extracted) {

        if (extracted.getName() != null
                && !extracted.getName().isBlank()) {
            draft.setName(extracted.getName());
        }

        if (extracted.getDescription() != null
                && !extracted.getDescription().isBlank()) {
            draft.setDescription(extracted.getDescription());
        }

        if (extracted.getPrice() != null
                && extracted.getPrice().signum() > 0) {
            draft.setPrice(extracted.getPrice());
        }

        if (extracted.getQuantity() != null
                && extracted.getQuantity() >= 0) {
            draft.setQuantity(extracted.getQuantity());
        }

        if (extracted.getCategory() != null
                && !extracted.getCategory().isBlank()) {
            draft.setCategory(extracted.getCategory());
        }
    }

    private String buildNextQuestion(AdminProductDraft draft) {

        if (draft.getName() == null || draft.getName().isBlank()) {
            return "Sure! Let's add a new product. What is the product name?";
        }

        if (draft.getDescription() == null
                || draft.getDescription().isBlank()) {
            return "What is the description of " + draft.getName() + "?";
        }

        if (draft.getPrice() == null) {
            return "What is the price of " + draft.getName() + "?";
        }

        if (draft.getQuantity() == null) {
            return "How many units are available in stock?";
        }

        if (draft.getCategory() == null
                || draft.getCategory().isBlank()) {
            return "Which category does this product belong to?";
        }

        return """
                Great! I have collected all the product details.

                Product: %s
                Description: %s
                Price: ₹%s
                Quantity: %d
                Category: %s

                Please upload the product image and review these details.
                Then click Confirm to add this product.
                """.formatted(
                draft.getName(),
                draft.getDescription(),
                draft.getPrice(),
                draft.getQuantity(),
                draft.getCategory()
        );
    }

    private String value(Object value) {
        return value == null ? "Not provided" : value.toString();
    }

    public AdminProductResponseHolder confirm(
            String adminId,
            String conversationId,
            String jwtToken,
            org.springframework.web.multipart.MultipartFile image) {

        String key = adminId + ":" + conversationId;

        AdminProductDraft draft = drafts.get(key);

        if (draft == null) {
            throw new IllegalArgumentException(
                    "Product draft not found. Please start again."
            );
        }

        if (!draft.isComplete()) {
            throw new IllegalArgumentException(
                    "Please provide all product details first."
            );
        }

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a product image."
            );
        }

        com.example.fashion_ai.admin.dto.AdminProductCreateRequest request =
                new com.example.fashion_ai.admin.dto.AdminProductCreateRequest();

        request.setName(draft.getName());
        request.setDescription(draft.getDescription());
        request.setPrice(draft.getPrice());
        request.setQuantity(draft.getQuantity());
        request.setCategory(draft.getCategory());
        request.setImage(image);

        com.example.fashion_ai.admin.dto.AdminProductResponse created =
                adminProductService.createProduct(jwtToken, request);

        // Remove the draft only after successful creation.
        drafts.remove(key);

        return new AdminProductResponseHolder(
                "Product created successfully!",
                created
        );
    }

    public void cancel(String adminId, String conversationId) {
        drafts.remove(adminId + ":" + conversationId);
    }

    public static class AdminProductResponseHolder {

        private String message;
        private com.example.fashion_ai.admin.dto.AdminProductResponse product;

        public AdminProductResponseHolder(
                String message,
                com.example.fashion_ai.admin.dto.AdminProductResponse product) {
            this.message = message;
            this.product = product;
        }

        public String getMessage() {
            return message;
        }

        public com.example.fashion_ai.admin.dto.AdminProductResponse getProduct() {
            return product;
        }
    }
}
