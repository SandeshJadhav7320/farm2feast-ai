
package com.example.fashion_ai.admin.dto;

public class AdminProductUpdateChatResponse {

    private String conversationId;
    private String reply;
    private AdminProductUpdateDraft draft;
    private boolean readyForConfirmation;

    public AdminProductUpdateChatResponse() {
    }

    public AdminProductUpdateChatResponse(
            String conversationId,
            String reply,
            AdminProductUpdateDraft draft,
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

    public AdminProductUpdateDraft getDraft() {
        return draft;
    }

    public void setDraft(AdminProductUpdateDraft draft) {
        this.draft = draft;
    }

    public boolean isReadyForConfirmation() {
        return readyForConfirmation;
    }

    public void setReadyForConfirmation(boolean readyForConfirmation) {
        this.readyForConfirmation = readyForConfirmation;
    }
}
