package com.example.producer;

import com.example.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderEventsTopic;

    public OrderEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topic.order-events:order-events}") String orderEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopic = orderEventsTopic;
    }

    /**
     * Publish OrderCreated event
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreated event for order: {}", event.getOrderId());
        kafkaTemplate.send(orderEventsTopic, "ORDER_CREATED", event);
        log.info("OrderCreated event published successfully");
    }

    /**
     * Publish OrderCancelled event
     */
    public void publishOrderCancelled(Long orderId) {
        log.info("Publishing OrderCancelled event for order: {}", orderId);

        // Simple event with just orderId
        String event = String.format("{\"orderId\": %d, \"eventType\": \"ORDER_CANCELLED\"}", orderId);
        kafkaTemplate.send(orderEventsTopic, "ORDER_CANCELLED", event);

        log.info("OrderCancelled event published successfully");
    }
}