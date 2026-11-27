
package com.example.fashion_ai.assistant.controller;

import com.example.fashion_ai.assistant.dto.AdminAssistantChatRequest;
import com.example.fashion_ai.assistant.dto.AdminAssistantChatResponse;
import com.example.fashion_ai.assistant.service.AdminAssistantService;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin-ai/assistant")
public class AdminAssistantController {

    private final AdminAssistantService assistantService;

    public AdminAssistantController(
            AdminAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    private void checkAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        boolean isAdmin = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException(
                    "Only administrators can use this assistant."
            );
        }
    }

    // ONE CHAT ENDPOINT FOR ALL OPERATIONS
    @PostMapping("/chat")
    public ResponseEntity<AdminAssistantChatResponse> chat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AdminAssistantChatRequest request) {

        checkAdmin();

        AdminAssistantChatResponse response =
                assistantService.chat(
                        jwt.getSubject(),
                        request.getConversationId(),
                        jwt.getTokenValue(),
                        request.getMessage()
                );

        return ResponseEntity.ok(response);
    }

    // CONFIRM ADD OR UPDATE PRODUCT
    @PostMapping(
            value = "/{conversationId}/confirm",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Object> confirm(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String conversationId,
            @RequestPart(value = "image", required = false)
            MultipartFile image) {

        checkAdmin();

        Object result = assistantService.confirm(
                jwt.getSubject(),
                conversationId,
                jwt.getTokenValue(),
                image
        );

        return ResponseEntity.ok(result);
    }

    // CANCEL CURRENT PRODUCT OPERATION
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<String> cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String conversationId) {

        checkAdmin();

        assistantService.cancel(
                jwt.getSubject(),
                conversationId
        );

        return ResponseEntity.ok(
                "Assistant conversation cancelled."
        );
    }
}
