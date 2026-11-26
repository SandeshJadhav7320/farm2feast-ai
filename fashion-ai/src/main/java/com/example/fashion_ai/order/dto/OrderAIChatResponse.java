
package com.example.fashion_ai.order.dto;

public class OrderAIChatResponse {

    private String reply;

    public OrderAIChatResponse() {}

    public OrderAIChatResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
