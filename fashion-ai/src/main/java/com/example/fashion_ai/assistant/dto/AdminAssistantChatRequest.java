
package com.example.fashion_ai.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminAssistantChatRequest {

    private String conversationId;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message is too long")
    private String message;

    public AdminAssistantChatRequest() {}

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
