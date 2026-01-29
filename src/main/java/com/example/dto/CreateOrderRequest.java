package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateOrderRequest {
    @NonNull
    private Long userId;
    @NonNull
    private List<OrderItemRequest> items;
}
