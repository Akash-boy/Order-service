package com.example.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@Builder  // Add this
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;  // Add this

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;  // Changed from 'Stock' to 'stock' (lowercase)

    private String category;  // Add this

    @Column(name = "created_at")
    private LocalDateTime createdAt;  // Add this

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // Add this
}