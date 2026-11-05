package com.example.fashion_ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.example.fashion_ai.service.CheckoutService;

public class CheckoutTools {

    private final CheckoutService checkoutService;
    private final Long customerId;
    private final String conversationId;
    private final String jwtToken;

    public CheckoutTools(
            CheckoutService checkoutService,
            Long customerId,
            String conversationId,
            String jwtToken
    ) {
        this.checkoutService = checkoutService;
        this.customerId = customerId;
        this.conversationId = conversationId;
        this.jwtToken = jwtToken;
    }

    @Tool(description = """
            Prepare checkout for the customer's actual shopping cart.
            Use this only when the customer wants to proceed with checkout
            and has supplied all required delivery details and payment method.
            This tool does not place the order.
            It saves a pending checkout and returns a summary for the
            customer to review and confirm.
            Payment method must be COD or ONLINE.
            """)
    public String prepareCheckout(
            @ToolParam(description = "Customer delivery name")
            String deliveryName,

            @ToolParam(description = "Customer delivery phone number")
            String phoneNumber,

            @ToolParam(description = "Complete delivery address")
            String deliveryAddress,

            @ToolParam(description = "Payment method: COD or ONLINE")
            String paymentMethod
    ) {
        return checkoutService.prepareCheckout(
                customerId,
                conversationId,
                jwtToken,
                deliveryName,
                phoneNumber,
                deliveryAddress,
                paymentMethod
        );
    }
}