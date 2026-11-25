
package com.example.fashion_ai.admin.dto;

public class AdminProductChatRequest {

    private String conversationId;
    private String message;

    public AdminProductChatRequest() {
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
