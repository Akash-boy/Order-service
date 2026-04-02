package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockRevertEvent {
    private Long orderId;
    private String reservationId;
    private String eventType; // "STOCK_REVERT"
    private String reason; // Reason for stock revert (e.g., "PAYMENT_FAILED", "ORDER_CANCELLED")
    private LocalDateTime revertedAt;
}
