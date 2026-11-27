
package com.example.fashion_ai.assistant.dto;

public class AdminAssistantChatResponse {

    private String conversationId;
    private String operation;
    private String reply;
    private Object draft;
    private boolean readyForConfirmation;

    public AdminAssistantChatResponse() {}

    public AdminAssistantChatResponse(
            String conversationId,
            String operation,
            String reply,
            Object draft,
            boolean readyForConfirmation) {

        this.conversationId = conversationId;
        this.operation = operation;
        this.reply = reply;
        this.draft = draft;
        this.readyForConfirmation = readyForConfirmation;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getOperation() {
        return operation;
    }

    public String getReply() {
        return reply;
    }

    public Object getDraft() {
        return draft;
    }

    public boolean isReadyForConfirmation() {
        return readyForConfirmation;
    }
}
