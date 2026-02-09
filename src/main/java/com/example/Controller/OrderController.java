package com.example.Controller;

import com.example.dto.CreateOrderRequest;
import com.example.entities.Order;
import com.example.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Place a new order
     */
    @PostMapping
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Received request to place order for user: {}", request.getUserId());
        Order order = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable("orderId") Long orderId) {
        log.info("Fetching order: {}", orderId);
        return orderService.getOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all orders for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Iterable<Order>> getUserOrders(@PathVariable("userId") Long userId) {
        log.info("Fetching orders for user: {}", userId);
        Iterable<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get all orders (admin)
     */
    @GetMapping
    public ResponseEntity<Iterable<Order>> getAllOrders() {
        log.info("Fetching all orders");
        Iterable<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable("orderId") Long orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }
}