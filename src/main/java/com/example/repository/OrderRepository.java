package com.example.repository;

import com.example.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    public Order findByOrderId(Long orderId);

    Iterable<Order> findByUserId(Long userId);
}
