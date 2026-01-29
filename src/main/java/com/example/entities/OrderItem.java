package com.example.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    // Each OrderItem belongs to one Product
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Each OrderItem belongs to one Order
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    private Double price;

}
