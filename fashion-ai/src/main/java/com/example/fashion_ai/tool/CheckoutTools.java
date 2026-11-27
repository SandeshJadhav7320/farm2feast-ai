package com.example.fashion_ai.tool;

import com.example.fashion_ai.dto.checkout.CheckoutItemDto;
import com.example.fashion_ai.service.CheckoutService;
import com.example.fashion_ai.service.ProductCollector;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

public class CheckoutTools {

    private final CheckoutService checkoutService;
    private final Long customerId;
    private final String conversationId;
    private final String jwtToken;
    private final ProductCollector productCollector;

    public CheckoutTools(
            CheckoutService checkoutService,
            Long customerId,
            String conversationId,
            String jwtToken,
            ProductCollector productCollector
    ) {
        this.checkoutService = checkoutService;
        this.customerId = customerId;
        this.conversationId = conversationId;
        this.jwtToken = jwtToken;
        this.productCollector = productCollector;
    }

    // EXISTING CART CHECKOUT TOOL

    @Tool(description = """
            Prepare checkout for the customer's actual shopping cart.

            Use this tool only when the customer wants to checkout
            products that are already in their cart.

            Collect delivery name, phone number, complete address,
            and payment method before calling this tool.

            Accepted payment methods are COD and ONLINE.

            This tool does not place the order.
            It prepares a pending checkout and asks the customer
            to type CONFIRM ORDER.
            """)
    public String prepareCheckout(
            @ToolParam(description = "Customer's delivery name")
            String deliveryName,

            @ToolParam(description = "Customer's 10-digit phone number")
            String phoneNumber,

            @ToolParam(description = "Customer's complete delivery address")
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
                paymentMethod,
                productCollector
        );
    }


    // NEW DIRECT ORDER TOOL

    @Tool(description = """
            Prepare a DIRECT ORDER without using the shopping cart.

            Use this tool when the customer explicitly wants to order
            one or more products directly without adding them to the cart.

            The items must contain actual product IDs from the product
            information provided in the conversation and the quantity
            requested by the customer.

            This tool supports both one product and multiple products.

            Collect delivery name, phone number, complete address,
            and payment method before calling this tool.

            Accepted payment methods are COD and ONLINE.

            This tool checks product availability and prepares a
            pending checkout. It does not place the order.

            The customer must review the summary and type CONFIRM ORDER
            before the order is created.

            Never invent product IDs or quantities.
            Never use prices supplied by the customer.
            Do not add these products to the shopping cart.
            """)
    public String prepareDirectCheckout(

            @ToolParam(description = """
                    List of products to order directly.
                    Each item must contain the actual productId
                    and the quantity requested by the customer.
                    """)
            List<CheckoutItemDto> items,

            @ToolParam(description = "Customer's delivery name")
            String deliveryName,

            @ToolParam(description = "Customer's 10-digit phone number")
            String phoneNumber,

            @ToolParam(description = "Customer's complete delivery address")
            String deliveryAddress,

            @ToolParam(description = "Payment method: COD or ONLINE")
            String paymentMethod
    ) {
        return checkoutService.prepareDirectCheckout(
                customerId,
                conversationId,
                jwtToken,
                deliveryName,
                phoneNumber,
                deliveryAddress,
                paymentMethod,
                items,
                productCollector
        );
    }
}