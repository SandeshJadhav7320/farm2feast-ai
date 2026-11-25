
package com.example.fashion_ai.admin.controller;

import com.example.fashion_ai.admin.dto.AdminProductCreateRequest;
import com.example.fashion_ai.admin.dto.AdminProductResponse;
import com.example.fashion_ai.admin.service.AdminProductService;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-ai/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(
            AdminProductService adminProductService) {

        this.adminProductService = adminProductService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminProductResponse createProduct(

            @AuthenticationPrincipal Jwt jwt,

            @Valid @ModelAttribute AdminProductCreateRequest request) {

        String jwtToken = jwt.getTokenValue();

        return adminProductService.createProduct(
                jwtToken,
                request
        );
    }
}
