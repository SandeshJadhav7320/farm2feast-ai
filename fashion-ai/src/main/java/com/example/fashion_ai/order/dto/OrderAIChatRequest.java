
package com.example.fashion_ai.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrderAIChatRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 2000)
    private String message;

    public OrderAIChatRequest() {}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
