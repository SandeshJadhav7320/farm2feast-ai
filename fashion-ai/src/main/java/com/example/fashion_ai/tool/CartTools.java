package com.example.fashion_ai.tool;

import com.example.fashion_ai.client.CartApiClient;
import com.example.fashion_ai.client.ProductApiClient;
import com.example.fashion_ai.dto.CartItemResponseDto;
import com.example.fashion_ai.dto.ProductPageResponse;
import com.example.fashion_ai.dto.StoreProduct;
import com.example.fashion_ai.service.ProductCollector;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

public class CartTools {

    private final CartApiClient cartApiClient;
    private final ProductApiClient productApiClient;
    private final String jwtToken;
    private final ProductCollector productCollector;

    public CartTools(
            CartApiClient cartApiClient,
            ProductApiClient productApiClient,
            String jwtToken,
            ProductCollector productCollector
    ) {
        this.cartApiClient = cartApiClient;
        this.productApiClient = productApiClient;
        this.jwtToken = jwtToken;
        this.productCollector = productCollector;
    }

    @Tool(description = """
            View the authenticated customer's shopping cart.
            Use this tool when the customer asks to see their cart,
            view cart items, or check their cart total.
            Never ask for a customer ID.
            """)
    public String getMyCart() {

        try {

            List<CartItemResponseDto> cartItems =
                    cartApiClient.getMyCart(jwtToken);

            if (cartItems == null || cartItems.isEmpty()) {
                return "Your shopping cart is currently empty.";
            }

            StringBuilder result = new StringBuilder();

            result.append("Your shopping cart:\n\n");

            for (CartItemResponseDto item : cartItems) {

                productCollector.add(item);

                result.append("Product: ")
                        .append(item.productName()).append("\n");

                result.append("Product ID: ")
                        .append(item.productId()).append("\n");

                result.append("Price: ₹")
                        .append(item.price()).append("\n");

                result.append("Quantity: ")
                        .append(item.quantity()).append("\n");

                result.append("Item Total: ₹")
                        .append(item.totalPrice()).append("\n\n");

                result.append("Cart Item ID: ")
                        .append(item.cartItemId())
                        .append("\n");
            }

            return result.toString();

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 401
                    || e.getStatusCode().value() == 403) {

                return "You are not authorized to view this cart.";
            }

            return "Unable to retrieve your cart right now.";

        } catch (Exception e) {

            e.printStackTrace();

            return "An unexpected error occurred while retrieving your cart.";
        }
    }

    @Tool(description = """
            Add a product to the authenticated customer's shopping cart.
            Use this tool when the customer explicitly asks to add a product.
            Provide the product name and requested quantity.
            The tool searches the real store catalogue to identify the product.
            If the product is ambiguous, it will return matching choices
            instead of adding the wrong product.
            """)
    public String addProductToCart(
            @ToolParam(description = "The product name requested by the customer")
            String productName,

            @ToolParam(description = "The number of units the customer wants to add")
            Integer quantity
    ) {

        if (productName == null || productName.isBlank()) {
            return "Please tell me which product you want to add.";
        }

        if (quantity == null || quantity <= 0) {
            return "Please provide a quantity greater than zero.";
        }

        try {

            ProductPageResponse page =
                    productApiClient.searchProducts(
                            jwtToken,
                            productName.trim(),
                            null,
                            null,
                            null,
                            null,
                            15
                    );

            if (page == null || page.getContent() == null
                    || page.getContent().isEmpty()) {
                return "I could not find that product in the store.";
            }

            List<StoreProduct> products = page.getContent();

            List<StoreProduct> exactMatches = products.stream()
                    .filter(product ->
                            product.getName() != null
                            && product.getName().trim()
                                    .equalsIgnoreCase(productName.trim()))
                    .toList();

            if (exactMatches.size() == 1) {
                return addMatchedProduct(exactMatches.get(0), quantity);
            }

            if (exactMatches.size() > 1 || products.size() > 1
                    || page.isHasNext()) {

                StringBuilder choices = new StringBuilder(
                        "I found multiple possible products. "
                        + "Please choose one:\n"
                );

                for (StoreProduct product : products) {
                    choices.append("- ")
                            .append(product.getName())
                            .append(" (Product ID: ")
                            .append(product.getId())
                            .append(", Price: ₹")
                            .append(product.getPrice())
                            .append(")\n");
                }

                return choices.toString();
            }

            return addMatchedProduct(products.get(0), quantity);

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 401
                    || e.getStatusCode().value() == 403) {
                return "You are not authorized to add products to this cart.";
            }

            return "Unable to search for the product right now.";

        } catch (Exception e) {

            e.printStackTrace();
            return "An unexpected error occurred while adding the product.";
        }
    }

    private String addMatchedProduct(
            StoreProduct product,
            Integer quantity
    ) {

        if (product.getQuantity() == null
                || product.getQuantity() <= 0) {
            return product.getName() + " is currently out of stock.";
        }

        if (quantity > product.getQuantity()) {
            return "Only " + product.getQuantity()
                    + " units of " + product.getName()
                    + " are currently available.";
        }

        try {

            String response = cartApiClient.addToCart(
                    jwtToken,
                    product.getId(),
                    quantity
            );

            productCollector.add(product);

            return response
                    + "\nProduct: " + product.getName()
                    + "\nQuantity added: " + quantity
                    + "\nUnit price: ₹" + product.getPrice();

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 401
                    || e.getStatusCode().value() == 403) {
                return "You are not authorized to modify this cart.";
            }

            return "The store could not add this product. "
                    + "Please check the available stock and try again.";
        }
    }

    @Tool(description = """
            Update the quantity of an existing item in the
            authenticated customer's shopping cart.
            First use getMyCart to find the actual cart item ID.
            The quantity parameter means the new final quantity,
            not the additional quantity.
            """)
    public String updateCartQuantity(
            @ToolParam(description = "The actual cart item ID from the customer's cart")
            Long cartItemId,

            @ToolParam(description = "The new final quantity")
            Integer quantity
    ) {

        if (cartItemId == null || cartItemId <= 0) {
            return "Invalid cart item ID.";
        }

        if (quantity == null || quantity <= 0) {
            return "Quantity must be greater than zero.";
        }

        try {

            return cartApiClient.updateCartItem(
                    jwtToken,
                    cartItemId,
                    quantity
            );

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 401
                    || e.getStatusCode().value() == 403) {
                return "You are not authorized to update this cart item.";
            }

            return "Unable to update the cart quantity. "
                    + "Please check the available stock.";

        } catch (Exception e) {

            e.printStackTrace();

            return "An unexpected error occurred while updating the cart.";
        }
    }

    @Tool(description = """
            Remove a product from the authenticated customer's
            shopping cart.
            First use getMyCart to find the actual cart item ID.
            Never use a product ID as a cart item ID.
            Use only when the customer explicitly requests removal.
            """)
    public String removeCartItem(
            @ToolParam(description = "The actual cart item ID from the customer's cart")
            Long cartItemId
    ) {

        if (cartItemId == null || cartItemId <= 0) {
            return "Invalid cart item ID.";
        }

        try {

            return cartApiClient.removeCartItem(
                    jwtToken,
                    cartItemId
            );

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 401
                    || e.getStatusCode().value() == 403) {
                return "You are not authorized to remove this cart item.";
            }

            if (e.getStatusCode().value() == 404) {
                return "The requested cart item was not found.";
            }

            return "Unable to remove this cart item right now.";

        } catch (Exception e) {

            e.printStackTrace();

            return "An unexpected error occurred while removing the cart item.";
        }
    }
}