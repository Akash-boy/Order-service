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
public class StockReleasedEvent {
    private Long orderId;
    private Long userId;
    private String reservationId;
    private String eventType; // "STOCK_RELEASED"
    private LocalDateTime releasedAt;
}
