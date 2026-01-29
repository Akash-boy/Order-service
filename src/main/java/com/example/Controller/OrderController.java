package com.example.Controller;

import com.example.dto.CreateOrderRequest;
import com.example.entities.Order;
import com.example.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Add this for validation

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Place an order
    @PostMapping
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // Get order by id
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable("orderId") Long orderId) {
        return orderService.getOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Fetch all orders of user
    @GetMapping
    public ResponseEntity<Iterable<Order>> getAllOrdersOfUser(@RequestParam Long userId) {
        Iterable<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}