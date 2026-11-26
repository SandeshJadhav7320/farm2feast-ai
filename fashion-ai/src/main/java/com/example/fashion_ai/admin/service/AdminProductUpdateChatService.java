
package com.example.fashion_ai.admin.service;

import com.example.fashion_ai.admin.dto.AdminProductResponse;
import com.example.fashion_ai.admin.dto.AdminProductUpdateChatResponse;
import com.example.fashion_ai.admin.dto.AdminProductUpdateDraft;
import com.example.fashion_ai.admin.dto.AdminProductUpdateExtraction;
import com.example.fashion_ai.admin.dto.AdminProductUpdateRequest;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AdminProductUpdateChatService {

    private final ChatClient chatClient;
    private final AdminProductService productService;

    private final ConcurrentMap<String, AdminProductUpdateDraft> drafts =
            new ConcurrentHashMap<>();

    public AdminProductUpdateChatService(
            ChatClient.Builder builder,
            AdminProductService productService) {

        this.chatClient = builder.build();
        this.productService = productService;
    }

    public AdminProductUpdateChatResponse chat(
            String adminId,
            String conversationId,
            String jwtToken,
            String message) {

        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Message cannot be empty."
            );
        }

        if (message.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Message is too long."
            );
        }

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        String key = adminId + ":" + conversationId;

        AdminProductUpdateDraft draft = drafts.computeIfAbsent(
                key,
                k -> new AdminProductUpdateDraft()
        );

        // Ask AI to extract product ID and explicitly requested changes.
        AdminProductUpdateExtraction extraction = extractChanges(
                message,
                draft
        );

        // First, identify and load the product.
        if (draft.getProductId() == null) {

            if (extraction == null || extraction.getProductId() == null) {
                return response(
                        conversationId,
                        "Please tell me the product ID you want to update.",
                        draft,
                        false
                );
            }

            Long productId = extraction.getProductId();

            AdminProductResponse product =
                    productService.getProductById(jwtToken, productId);

            draft.setProductId(product.getId());

            draft.setOriginalName(product.getName());
            draft.setOriginalDescription(product.getDescription());
            draft.setOriginalPrice(product.getPrice());
            draft.setOriginalQuantity(product.getQuantity());
            draft.setOriginalCategory(product.getCategory());

            // Initially, proposed values are the existing values.
            draft.setName(product.getName());
            draft.setDescription(product.getDescription());
            draft.setPrice(product.getPrice());
            draft.setQuantity(product.getQuantity());
            draft.setCategory(product.getCategory());
        }

        // Apply only fields explicitly mentioned by the admin.
        if (extraction != null) {
            applyChanges(draft, extraction);
        }

        // Ask for confirmation, but do not update yet.
        if (isConfirmationMessage(message)) {

            if (!draft.isHasChanges()) {
                return response(
                        conversationId,
                        "No changes have been requested. Tell me what you want to change first.",
                        draft,
                        false
                );
            }

            return response(
                    conversationId,
                    buildSummary(draft) +
                    "\n\nPlease confirm by calling the update confirmation endpoint.",
                    draft,
                    true
            );
        }

        if (!draft.isHasChanges()) {
            return response(
                    conversationId,
                    "I found product ID " + draft.getProductId() +
                    ". What would you like to change? You can change its name, description, price, quantity or category.",
                    draft,
                    false
            );
        }

        return response(
                conversationId,
                buildSummary(draft) +
                "\n\nWould you like to confirm these changes? Reply with 'confirm' when you are ready.",
                draft,
                false
        );
    }

    private AdminProductUpdateExtraction extractChanges(
            String message,
            AdminProductUpdateDraft draft) {

        String currentProduct;

        if (draft.getProductId() == null) {
            currentProduct = "No product has been selected yet.";
        } else {
            currentProduct =
                    "Product ID: " + draft.getProductId() +
                    "\nCurrent name: " + draft.getName() +
                    "\nCurrent description: " + draft.getDescription() +
                    "\nCurrent price: " + draft.getPrice() +
                    "\nCurrent quantity: " + draft.getQuantity() +
                    "\nCurrent category: " + draft.getCategory();
        }

        return chatClient.prompt()
                .system("""
                    You extract product update information from an admin message.

                    Return only fields explicitly requested by the admin.
                    Do not guess missing values.
                    Do not change fields that the admin did not mention.
                    Extract productId only when the admin clearly provides an ID.

                    Supported fields:
                    productId, name, description, price, quantity, category.

                    For fields not mentioned, return null.
                    Do not perform any product update.
                    """)
                .user("""
                    Existing product information:
                    %s

                    Admin message:
                    %s
                    """.formatted(currentProduct, message))
                .call()
                .entity(AdminProductUpdateExtraction.class);
    }

    private void applyChanges(
            AdminProductUpdateDraft draft,
            AdminProductUpdateExtraction extraction) {

        if (extraction.getName() != null) {
            draft.setName(extraction.getName());
            draft.setHasChanges(true);
        }

        if (extraction.getDescription() != null) {
            draft.setDescription(extraction.getDescription());
            draft.setHasChanges(true);
        }

        if (extraction.getPrice() != null) {
            draft.setPrice(extraction.getPrice());
            draft.setHasChanges(true);
        }

        if (extraction.getQuantity() != null) {
            draft.setQuantity(extraction.getQuantity());
            draft.setHasChanges(true);
        }

        if (extraction.getCategory() != null) {
            draft.setCategory(extraction.getCategory());
            draft.setHasChanges(true);
        }
    }

    private boolean isConfirmationMessage(String message) {
        String text = message.trim().toLowerCase();

        return text.equals("confirm")
                || text.equals("yes")
                || text.equals("yes confirm")
                || text.equals("confirm update")
                || text.equals("update it")
                || text.equals("save changes");
    }

    private String buildSummary(AdminProductUpdateDraft draft) {

        return "Here are the proposed changes for product ID "
                + draft.getProductId() + ":\n\n"

                + "Name: " + draft.getOriginalName()
                + " -> " + draft.getName() + "\n"

                + "Description: " + draft.getOriginalDescription()
                + " -> " + draft.getDescription() + "\n"

                + "Price: " + draft.getOriginalPrice()
                + " -> " + draft.getPrice() + "\n"

                + "Quantity: " + draft.getOriginalQuantity()
                + " -> " + draft.getQuantity() + "\n"

                + "Category: " + draft.getOriginalCategory()
                + " -> " + draft.getCategory();
    }

    private AdminProductUpdateChatResponse response(
            String conversationId,
            String reply,
            AdminProductUpdateDraft draft,
            boolean ready) {

        return new AdminProductUpdateChatResponse(
                conversationId,
                reply,
                draft,
                ready
        );
    }

    public AdminProductResponse confirm(
            String adminId,
            String conversationId,
            String jwtToken,
            MultipartFile image) {

        String key = adminId + ":" + conversationId;

        AdminProductUpdateDraft draft = drafts.get(key);

        if (draft == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Update conversation not found or expired."
            );
        }

        if (!draft.isHasChanges()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There are no changes to save."
            );
        }

        AdminProductUpdateRequest request =
                new AdminProductUpdateRequest();

        // Send every required field to the Store backend.
        // This is important because its update API requires all text fields.
        request.setName(draft.getName());
        request.setDescription(draft.getDescription());
        request.setPrice(draft.getPrice());
        request.setQuantity(draft.getQuantity());
        request.setCategory(draft.getCategory());

        // Image is optional. If null, the existing image remains unchanged.
        request.setImage(image);

        AdminProductResponse updatedProduct =
                productService.updateProduct(
                        jwtToken,
                        draft.getProductId(),
                        request
                );

        drafts.remove(key);

        return updatedProduct;
    }

    public void cancel(String adminId, String conversationId) {
        drafts.remove(adminId + ":" + conversationId);
    }
}
