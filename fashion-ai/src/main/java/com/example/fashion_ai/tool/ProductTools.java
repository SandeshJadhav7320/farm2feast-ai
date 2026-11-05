package com.example.fashion_ai.tool;

import com.example.fashion_ai.client.ProductApiClient;
import com.example.fashion_ai.dto.StoreProduct;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.client.RestClientResponseException;

public class ProductTools {

    private final ProductApiClient productApiClient;
    private final String jwtToken;

    public ProductTools(
            ProductApiClient productApiClient,
            String jwtToken) {

        this.productApiClient = productApiClient;
        this.jwtToken = jwtToken;
    }

    @Tool(description = """
            Check the current stock availability of a Makhana product.
            Use this tool when a customer asks whether a product is
            available, how many units are available, or asks about stock.
            You must use the product ID from the supplied product list.
            """)
    public String checkProductStock(
            @ToolParam(description = "The actual product ID from the product list")
            Long productId) {

        if (productId == null || productId <= 0) {
            return "Invalid product ID.";
        }

        try {
            StoreProduct product =
                    productApiClient.getProductById(jwtToken, productId);

            if (product == null) {
                return "Product not found.";
            }

            if (product.getQuantity() == null
                    || product.getQuantity() <= 0) {

                return product.getName() + " is currently out of stock.";
            }

            return """
                    Product: %s
                    Product ID: %d
                    Available quantity: %d
                    Stock status: In stock
                    """.formatted(
                    product.getName(),
                    product.getId(),
                    product.getQuantity()
            );

        } catch (RestClientResponseException exception) {

            System.err.println(
                    "Store API error: " + exception.getStatusCode()
            );

            System.err.println(
                    "Store API response: " + exception.getResponseBodyAsString()
            );

            if (exception.getStatusCode().value() == 404) {
                return "The requested product was not found.";
            }

            if (exception.getStatusCode().value() == 401
                    || exception.getStatusCode().value() == 403) {
                return "You are not authorized to check this product.";
            }

            return "Unable to check product stock at the moment.";

        } catch (Exception exception) {

            exception.printStackTrace();

            return "An unexpected error occurred while checking product stock.";
        }
    }

    @Tool(description = """
            Get complete details of a Makhana product.
            Use this tool when a customer asks about a product's
            name, price, description, category or general information.
            Use the actual product ID from the available product list.
            """)
    public String getProductDetails(
            @ToolParam(description = "The actual product ID")
            Long productId) {

        if (productId == null || productId <= 0) {
            return "Invalid product ID.";
        }

        try {
            StoreProduct product =
                    productApiClient.getProductById(jwtToken, productId);

            if (product == null) {
                return "Product not found.";
            }

            return """
                    Product Name: %s
                    Product ID: %d
                    Description: %s
                    Price: ₹%s
                    Category: %s
                    Available Quantity: %d
                    """.formatted(
                    product.getName(),
                    product.getId(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getCategory(),
                    product.getQuantity()
            );

        } catch (RestClientResponseException exception) {

            if (exception.getStatusCode().value() == 404) {
                return "Product not found.";
            }

            return "Unable to retrieve product details right now.";

        } catch (Exception exception) {

            exception.printStackTrace();
            return "An unexpected error occurred while retrieving product details.";
        }
    }
}