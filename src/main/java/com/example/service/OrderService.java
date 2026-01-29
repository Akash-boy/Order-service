package com.example.service;

import com.example.entities.Order;
import com.example.entities.OrderItem;
import com.example.entities.Product;
import com.example.entities.Users;
import com.example.exception.InsufficientStockException;
import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderItemRequest;

import com.example.producer.OrderEventProducer;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;  // ADD THIS - to reduce stock
    @Autowired
    private final OrderEventProducer kafkaProducer;  // ADD THIS - to publish events

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository,OrderEventProducer kafkaProducer) {
        this.orderRepository = orderRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Transactional  // ADD THIS - ensures all DB operations succeed or rollback together
    public Order placeOrder(CreateOrderRequest request) {
        // 1. Validate User
        Users user = validateUser(request.getUserId());

        // 2. Validate Products and create order items
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemRequest item : request.getItems()) {
            Product product = validateProduct(item.getProductId());

            // 3. Check stock availability
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName()
                );
            }

            // Calculate item total
            double itemTotal = product.getPrice() * item.getQuantity();
            totalAmount += itemTotal;

            // Create order item (without setting order yet)
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice())  // Store price at time of order
                    .build();

            orderItems.add(orderItem);
        }

        // 4. Create order
        Order order = Order.builder()
                .userId(user.getUserId())
                .status("PLACED")
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .build();

        // 5. Link order items to order (IMPORTANT!)
        for (OrderItem item : orderItems) {
            item.setOrder(order);  // Set the order reference
        }
        order.setOrderItems(orderItems);

        // 6. Save order (this will also save order items due to cascade)
        order = orderRepository.save(order);

        // 7. Reduce stock for each product (IMPORTANT!)
        for (OrderItemRequest item : request.getItems()) {
            productService.reduceStock(item.getProductId(), item.getQuantity());
        }

        // 8. Publish event (when you enable Kafka)
        kafkaProducer.publishOrderCreated(OrderInfoEventToPublish(order));

        return order;
    }
    private CreateOrderRequest OrderInfoEventToPublish(Order order) {
        // Convert Order entity to CreateOrderRequest DTO for publishing
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            itemRequests.add(new OrderItemRequest(item.getProduct().getProductId(), item.getQuantity()));
        }
        return new CreateOrderRequest(order.getUserId(), itemRequests);
    }

    // Get order by id
    public Optional<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }

    // Fetch all orders of user
    public Iterable<Order> getOrdersByUserId(Long userId) {
        // Validate user exists
        validateUser(userId);
        return orderRepository.findByUserId(userId);
    }

    // Get all orders (admin function)
    public Iterable<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Cancel order
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PLACED".equals(order.getStatus())) {
            throw new RuntimeException("Only PLACED orders can be cancelled");
        }

        // Restore stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // Update order status
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // Private helper methods
    private Users validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Product validateProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}