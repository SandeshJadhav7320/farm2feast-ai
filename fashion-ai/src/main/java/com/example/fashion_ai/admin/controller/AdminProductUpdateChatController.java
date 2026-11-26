
package com.example.fashion_ai.admin.controller;

import com.example.fashion_ai.admin.dto.AdminProductChatRequest;
import com.example.fashion_ai.admin.dto.AdminProductResponse;
import com.example.fashion_ai.admin.dto.AdminProductUpdateChatResponse;
import com.example.fashion_ai.admin.service.AdminProductUpdateChatService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin-ai/update-chat")
public class AdminProductUpdateChatController {

    private final AdminProductUpdateChatService updateChatService;

    public AdminProductUpdateChatController(
            AdminProductUpdateChatService updateChatService) {

        this.updateChatService = updateChatService;
    }

    // 1. Send a message to the product update AI.
    @PostMapping
    public AdminProductUpdateChatResponse chat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AdminProductChatRequest request) {

        return updateChatService.chat(
                jwt.getSubject(),
                request.getConversationId(),
                jwt.getTokenValue(),
                request.getMessage()
        );
    }

    // 2. Confirm and save the product changes.
    // Image is optional.
    @PostMapping("/{conversationId}/confirm")
    public AdminProductResponse confirm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String conversationId,
            @RequestPart(value = "image", required = false)
            MultipartFile image) {

        return updateChatService.confirm(
                jwt.getSubject(),
                conversationId,
                jwt.getTokenValue(),
                image
        );
    }

    // 3. Cancel the update conversation.
    @DeleteMapping("/{conversationId}")
    public String cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String conversationId) {

        updateChatService.cancel(
                jwt.getSubject(),
                conversationId
        );

        return "Product update cancelled.";
    }
}
