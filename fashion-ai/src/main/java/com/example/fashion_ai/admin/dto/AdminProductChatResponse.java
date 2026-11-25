
package com.example.fashion_ai.admin.dto;

public class AdminProductChatResponse {

    private String conversationId;
    private String reply;
    private AdminProductDraft draft;
    private boolean readyForConfirmation;

    public AdminProductChatResponse() {
    }

    public AdminProductChatResponse(
            String conversationId,
            String reply,
            AdminProductDraft draft,
            boolean readyForConfirmation) {

        this.conversationId = conversationId;
        this.reply = reply;
        this.draft = draft;
        this.readyForConfirmation = readyForConfirmation;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public AdminProductDraft getDraft() {
        return draft;
    }

    public void setDraft(AdminProductDraft draft) {
        this.draft = draft;
    }

    public boolean isReadyForConfirmation() {
        return readyForConfirmation;
    }

    public void setReadyForConfirmation(boolean readyForConfirmation) {
        this.readyForConfirmation = readyForConfirmation;
    }
}
