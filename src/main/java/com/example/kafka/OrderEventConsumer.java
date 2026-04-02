package com.example.kafka;

import com.example.dto.OrderCompletedRequest;
import com.example.dto.StockReleasedEvent;
import com.example.dto.StockReservationFailedEvent;
import com.example.dto.StockRevertEvent;
import com.example.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topic.inventory-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryEvents(
            @Payload Map<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        String eventType = (String) message.get("eventType");

        log.info("Received event: {} from topic: {}, partition: {}, offset: {}",
                eventType, topic, partition, offset);

        switch (eventType) {
            case "STOCK_RESERVATION_FAILED" -> handleStockReservationFailed(message);
            case "STOCK_RELEASED"           -> handleStockReleased(message);
            case "STOCK_REVERTED"           -> handleStockReverted(message);
            default -> log.debug("Skipping unknown event type: {}", eventType);
        }
    }

    private void handleStockReservationFailed(Map<String, Object> message) {
        StockReservationFailedEvent event = objectMapper.convertValue(message, StockReservationFailedEvent.class);

        log.info("Handling StockReservationFailed for order: {}, reason: {}",
                event.getOrderId(), event.getReason());

        orderService.cancelOrder(event.getOrderId());
        orderEventProducer.publishOrderCancelled(event.getOrderId());
    }

    private void handleStockReleased(Map<String, Object> message) {
        StockReleasedEvent event = objectMapper.convertValue(message, StockReleasedEvent.class);

        log.info("Handling StockReleased for order: {}", event.getOrderId());

        OrderCompletedRequest request = OrderCompletedRequest.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .build();

        orderService.completeOrder(request);
    }

    private void handleStockReverted(Map<String, Object> message) {
        StockRevertEvent event = objectMapper.convertValue(message, StockRevertEvent.class);

        log.info("Handling StockReverted for order: {}, reason: {}",
                event.getOrderId(), event.getReason());

        orderService.revertStockForOrder(event.getOrderId(), event.getReason());
    }
}