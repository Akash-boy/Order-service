package com.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    @NotNull(message = "User ID is required")
    private Long userId;
}
