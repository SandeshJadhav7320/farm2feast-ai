package com.example.fashion_ai.controller;

import com.example.fashion_ai.dto.ConversationRequest;
import com.example.fashion_ai.dto.ChatResponse;
import com.example.fashion_ai.service.ConversationService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService) {

        this.conversationService = conversationService;
    }

   @PostMapping("/chat")
    public ChatResponse chat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ConversationRequest request) {

        Number userIdClaim = jwt.getClaim("userId");
        Long customerId = userIdClaim.longValue();

        String jwtToken = jwt.getTokenValue();

        String aiResponse = conversationService.chat(
                customerId,
                request.getConversationId(),
                request.getMessage(),
                jwtToken
        );

        return new ChatResponse(aiResponse);
    }
}