package com.example.enums;

public enum OrderStatus {
    PENDING, // Order created, waiting for inventory reservation
    PLACED,  // Inventory reserved, waiting for payment
    INVENTORY_RESERVED, // Inventory reserved, waiting for payment
    PAYMENT_PENDING,   // Payment initiated
    CONFIRMED,         // Payment successful
    SHIPPED,           // Order shipped
    DELIVERED,         // Order delivered
    CANCELLED,         // Order cancelled
    FAILED             // Order failed
}