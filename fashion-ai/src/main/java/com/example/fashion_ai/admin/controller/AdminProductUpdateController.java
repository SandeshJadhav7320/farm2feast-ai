
package com.example.fashion_ai.admin.controller;

import com.example.fashion_ai.admin.dto.AdminProductResponse;
import com.example.fashion_ai.admin.dto.AdminProductUpdateRequest;
import com.example.fashion_ai.admin.service.AdminProductService;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-ai/products")
public class AdminProductUpdateController {

    private final AdminProductService adminProductService;

    public AdminProductUpdateController(
            AdminProductService adminProductService) {

        this.adminProductService = adminProductService;
    }

    @GetMapping("/{id}")
    public AdminProductResponse getProductById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        return adminProductService.getProductById(
                jwt.getTokenValue(),
                id
        );
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public AdminProductResponse updateProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @ModelAttribute AdminProductUpdateRequest request) {

        return adminProductService.updateProduct(
                jwt.getTokenValue(),
                id,
                request
        );
    }
}
