package com.example.kafka;

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

        kafkaTemplate.send(orderEventsTopic, "ORDER_CREATED", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Failed to publish OrderCreated event for order: {}. Error: {}",
                                event.getOrderId(), ex.getMessage(), ex);
                    } else {
                        log.info("✅ OrderCreated event published — topic: {}, partition: {}, offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public void publishOrderCancelled(Long orderId) {
        log.info("Publishing OrderCancelled event for order: {}", orderId);
        String event = String.format("{\"orderId\": %d, \"eventType\": \"ORDER_CANCELLED\"}", orderId);

        kafkaTemplate.send(orderEventsTopic, "ORDER_CANCELLED", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Failed to publish OrderCancelled for order: {}. Error: {}",
                                orderId, ex.getMessage(), ex);
                    } else {
                        log.info("✅ OrderCancelled event published for order: {}", orderId);
                    }
                });
    }

    public void publishOrderCompleted(Long orderId, Long userId) {
        log.info("Publishing OrderCompleted event for order: {}", orderId);
        String event = String.format("{\"orderId\": %d, \"userId\": %d, \"eventType\": \"ORDER_COMPLETED\"}",
                orderId, userId);

        kafkaTemplate.send(orderEventsTopic, "ORDER_COMPLETED", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Failed to publish OrderCompleted for order: {}. Error: {}",
                                orderId, ex.getMessage(), ex);
                    } else {
                        log.info("✅ OrderCompleted event published for order: {}", orderId);
                    }
                });
    }
}