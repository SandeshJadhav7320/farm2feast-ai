
package com.example.fashion_ai.admin.client;

import com.example.fashion_ai.admin.dto.AdminProductCreateRequest;
import com.example.fashion_ai.admin.dto.AdminProductResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Component
public class AdminProductApiClient {

    private final RestClient restClient;

    public AdminProductApiClient(
            @Value("${store.api.base-url}") String storeApiBaseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(storeApiBaseUrl)
                .build();
    }

    public AdminProductResponse createProduct(
            String jwtToken,
            AdminProductCreateRequest request) throws IOException {

        MultiValueMap<String, Object> parts =
                new LinkedMultiValueMap<>();

        // Add normal product fields.
        parts.add("name", request.getName());
        parts.add("description", request.getDescription());
        parts.add("price", request.getPrice().toString());
        parts.add("quantity", request.getQuantity().toString());
        parts.add("category", request.getCategory());

        // Prepare the uploaded image.
        ByteArrayResource imageResource =
                new ByteArrayResource(request.getImage().getBytes()) {

                    @Override
                    public String getFilename() {
                        return request.getImage().getOriginalFilename();
                    }
                };

        HttpHeaders imageHeaders = new HttpHeaders();

        imageHeaders.setContentType(
                MediaType.parseMediaType(
                        request.getImage().getContentType()
                )
        );

        imageHeaders.setContentDispositionFormData(
                "image",
                request.getImage().getOriginalFilename()
        );

        HttpEntity<ByteArrayResource> imageEntity =
                new HttpEntity<>(imageResource, imageHeaders);

        parts.add("image", imageEntity);

        // Send the request to the existing Store backend.
        return restClient.post()
                .uri("/api/admin/products")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .body(parts)
                .retrieve()
                .body(AdminProductResponse.class);
    }
}
