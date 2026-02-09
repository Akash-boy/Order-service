package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailabilityResponse {
    private boolean available;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal currentPrice;
    private Integer availableQuantity;
    private String message;
}