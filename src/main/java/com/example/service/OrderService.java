package com.example.service;

import com.example.client.InventoryServiceClient;
import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderCreatedEvent;
import com.example.dto.OrderItemRequest;
import com.example.dto.ProductAvailabilityResponse;
import com.example.entities.Order;
import com.example.entities.OrderItem;
import com.example.entities.Users;
import com.example.enums.OrderStatus;
import com.example.exception.InsufficientStockException;
import com.example.exception.OrderException;
import com.example.exception.UserNotFoundException;
import com.example.producer.OrderEventProducer;
import com.example.repository.OrderRepository;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryClient;
    private final OrderEventProducer orderEventProducer;

    @Autowired
    public OrderService(
            UserRepository userRepository,
            OrderRepository orderRepository,
            InventoryServiceClient inventoryClient,
            OrderEventProducer orderEventProducer) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.orderEventProducer = orderEventProducer;
    }

    /**
     * Place a new order
     *
     * Flow:
     * 1. Validate user
     * 2. Check product availability from Inventory Service
     * 3. Create order with PENDING status
     * 4. Create order items with product snapshots
     * 5. Save order
     * 6. Publish OrderCreated event to Kafka
     * 7. Inventory Service will reserve stock when it receives the event
     */
    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        log.info("Placing order for user: {}", request.getUserId());

        // 1. Validate user
        Users user = validateUser(request.getUserId());

        // 2. Check availability for all products from Inventory Service
        List<ProductAvailabilityResponse> availabilityResponses = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductAvailabilityResponse availability = inventoryClient.checkProductAvailability(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity()
            );

            // If product is not available, throw exception
            if (!availability.isAvailable()) {
                log.error("Product {} not available. Requested: {}, Available: {}",
                        availability.getProductId(),
                        itemRequest.getQuantity(),
                        availability.getAvailableQuantity());

                throw new InsufficientStockException(
                        String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                                availability.getProductName(),
                                availability.getAvailableQuantity() != null ? availability.getAvailableQuantity() : 0,
                                itemRequest.getQuantity())
                );
            }

            availabilityResponses.add(availability);
        }

        // 3. Create Order entity
        Order order = Order.builder()
                .userId(user.getUserId())
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .build();

        // 4. Create OrderItems with product snapshots
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest itemRequest = request.getItems().get(i);
            ProductAvailabilityResponse availability = availabilityResponses.get(i);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(availability.getProductId())
                    .productName(availability.getProductName())
                    .productSku(availability.getProductSku())
                    .quantity(itemRequest.getQuantity())
                    .priceAtOrder(availability.getCurrentPrice())
                    .build();

            // Subtotal is calculated automatically via @PrePersist
            orderItems.add(orderItem);

            // Calculate total
            BigDecimal itemTotal = availability.getCurrentPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // 5. Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} for user: {}", savedOrder.getOrderId(), savedOrder.getUserId());

        // 6. Publish OrderCreated event to Kafka
        // Inventory Service will consume this and reserve stock
        try {
            OrderCreatedEvent event = buildOrderCreatedEvent(savedOrder);
            orderEventProducer.publishOrderCreated(event);
            log.info("OrderCreated event published for order: {}", savedOrder.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreated event for order {}: {}",
                    savedOrder.getOrderId(), e.getMessage(), e);
            // Order is still created even if event publishing fails
            // In production, you'd want retry logic or dead letter queue
        }

        return savedOrder;
    }

    /**
     * Get order by ID
     */
    public Optional<Order> getOrder(Long orderId) {
        log.info("Fetching order by ID: {}", orderId);
        return orderRepository.findById(orderId);
    }

    /**
     * Get all orders for a user
     */
    public Iterable<Order> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user: {}", userId);
        validateUser(userId);
        return orderRepository.findByUserId(userId);
    }

    /**
     * Get all orders (admin function)
     */
    public Iterable<Order> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll();
    }

    /**
     * Cancel order
     * This will publish OrderCancelled event
     * Inventory Service will release reserved stock
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));

        // Only allow cancellation of PENDING or INVENTORY_RESERVED orders
        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.INVENTORY_RESERVED &&
                order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new OrderException("Only pending orders can be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);

        // Publish OrderCancelled event
        // Inventory Service will release the stock
        try {
            orderEventProducer.publishOrderCancelled(orderId);
            log.info("OrderCancelled event published for order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to publish OrderCancelled event: {}", e.getMessage(), e);
        }

        return cancelledOrder;
    }

    /**
     * Update order status
     * Called by Kafka consumers when events are received
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    /**
     * Validate user exists
     */
    private Users validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Build OrderCreatedEvent from Order entity
     */
    private OrderCreatedEvent buildOrderCreatedEvent(Order order) {
        List<OrderCreatedEvent.OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(item -> OrderCreatedEvent.OrderItemDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productSku(item.getProductSku())
                        .quantity(item.getQuantity())
                        .price(item.getPriceAtOrder())
                        .build())
                .collect(Collectors.toList());

        return OrderCreatedEvent.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .items(itemDtos)
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .eventType("ORDER_CREATED")
                .build();
    }
}