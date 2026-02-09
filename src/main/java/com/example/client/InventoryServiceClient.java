package com.example.client;

import com.example.dto.ProductAvailabilityRequest;
import com.example.dto.ProductAvailabilityResponse;
import com.example.exception.InventoryServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class InventoryServiceClient {

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public InventoryServiceClient(
            RestTemplate restTemplate,
            @Value("${inventory.service.url:http://localhost:8082}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    /**
     * Check if product is available in requested quantity
     */
    public ProductAvailabilityResponse checkProductAvailability(Long productId, Integer quantity) {
        try {
            String url = inventoryServiceUrl + "/api/v1/inventory/check-availability";

            ProductAvailabilityRequest request = ProductAvailabilityRequest.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build();

            log.info("Checking availability for product {} with quantity {}", productId, quantity);

            ProductAvailabilityResponse response = restTemplate.postForObject(
                    url,
                    request,
                    ProductAvailabilityResponse.class
            );

            log.info("Availability check response: available={}, message={}",
                    response != null && response.isAvailable(),
                    response != null ? response.getMessage() : "null");

            return response;

        } catch (Exception e) {
            log.error("Error checking product availability: {}", e.getMessage(), e);

            throw new InventoryServiceException(
                    "Failed to check product availability: " + e.getMessage(), e
            );
        }
    }
}