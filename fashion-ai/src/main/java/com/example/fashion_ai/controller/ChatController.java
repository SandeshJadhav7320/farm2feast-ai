
package com.example.fashion_ai.controller;

import com.example.fashion_ai.dto.FashionRequest;
import com.example.fashion_ai.dto.OutfitRecommendation;
import com.example.fashion_ai.service.ChatService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/fashion-recommendation")
    public OutfitRecommendation recommend(
            @RequestBody FashionRequest request) {

        return chatService.chat(request);
    }
}
