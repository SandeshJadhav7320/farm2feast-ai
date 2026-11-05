package com.example.fashion_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ConversationRequest {

    @NotBlank(message = "Conversation ID is required.")
    @Size(max = 100, message = "Conversation ID cannot exceed 100 characters.")
    private String conversationId;

    @NotBlank(message = "Message cannot be empty.")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters.")
    private String message;

    public ConversationRequest() {
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}