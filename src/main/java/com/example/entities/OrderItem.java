package com.example.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    // REMOVED: @ManyToOne relationship to Product
    // ADDED: Simple reference fields

    @Column(nullable = false)
    private Long productId;  // Reference to Inventory Service product

    @Column(nullable = false)
    private String productName;  // Snapshot at order time

    @Column(nullable = false)
    private String productSku;  // Snapshot for reference

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtOrder;  // Price when order was created

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;  // quantity * priceAtOrder

    // Relationship to Order (unchanged)
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Calculate subtotal before saving
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (priceAtOrder != null && quantity != null) {
            this.subtotal = priceAtOrder.multiply(BigDecimal.valueOf(quantity));
        }
    }
}