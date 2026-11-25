
package com.example.fashion_ai.admin.controller;

import com.example.fashion_ai.admin.dto.AdminProductChatRequest;
import com.example.fashion_ai.admin.dto.AdminProductChatResponse;
import com.example.fashion_ai.admin.service.AdminProductChatService;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin-ai")
public class AdminProductChatController {

    private final AdminProductChatService chatService;

    public AdminProductChatController(
            AdminProductChatService chatService) {
        this.chatService = chatService;
    }

    // 1. Send a normal message to AI.
    @PostMapping("/chat")
    public AdminProductChatResponse chat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AdminProductChatRequest request) {

        return chatService.chat(
                jwt.getSubject(),
                request.getConversationId(),
                request.getMessage()
        );
    }

    // 2. Upload the image and confirm product creation.
    @PostMapping(
            value = "/chat/{conversationId}/confirm",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public AdminProductChatService.AdminProductResponseHolder confirm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String conversationId,
            @RequestPart("image") MultipartFile image) {

        return chatService.confirm(
                jwt.getSubject(),
                conversationId,
                jwt.getTokenValue(),
                image
        );
    }

    // 3. Cancel an unfinished product draft.
    @DeleteMapping("/chat/{conversationId}")
    public String cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String conversationId) {

        chatService.cancel(
                jwt.getSubject(),
                conversationId
        );

        return "Product draft cancelled.";
    }
}
