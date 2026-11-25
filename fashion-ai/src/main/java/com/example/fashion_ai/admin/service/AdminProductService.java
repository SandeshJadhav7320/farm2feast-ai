
package com.example.fashion_ai.admin.service;

import com.example.fashion_ai.admin.client.AdminProductApiClient;
import com.example.fashion_ai.admin.dto.AdminProductCreateRequest;
import com.example.fashion_ai.admin.dto.AdminProductResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@Service
public class AdminProductService {

    private final AdminProductApiClient adminProductApiClient;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public AdminProductService(
            AdminProductApiClient adminProductApiClient) {

        this.adminProductApiClient = adminProductApiClient;
    }

    public AdminProductResponse createProduct(
            String jwtToken,
            AdminProductCreateRequest request) {

        // Check whether an image was uploaded.
        if (request.getImage() == null ||
                request.getImage().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload a product image."
            );
        }

        // Check image format.
        String contentType = request.getImage().getContentType();

        if (contentType == null ||
                !ALLOWED_IMAGE_TYPES.contains(contentType)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPG, PNG and WEBP images are allowed."
            );
        }

        // Check image size (5 MB maximum).
        if (request.getImage().getSize() > 5 * 1024 * 1024) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image size must not exceed 5 MB."
            );
        }

        try {

            return adminProductApiClient.createProduct(
                    jwtToken,
                    request
            );

        } catch (IOException exception) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read the product image."
            );
        }
    }
}
