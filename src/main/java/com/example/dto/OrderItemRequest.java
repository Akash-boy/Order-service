package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderItemRequest {
    @NonNull
    private Long productId;
    @NonNull
    private Integer quantity;


}
