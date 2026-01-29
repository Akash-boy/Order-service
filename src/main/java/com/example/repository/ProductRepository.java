package com.example.repository;

import com.example.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductId(Long productId);
    // Find products by category
    List<Product> findByCategory(String category);

    // Find products by name (case-insensitive, partial match)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find products with stock greater than 0
    List<Product> findByStockGreaterThan(Integer stock);

    // Find product by exact name
    Optional<Product> findByName(String name);
}
