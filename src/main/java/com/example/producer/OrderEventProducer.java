package com.example.producer;
import com.example.dto.CreateOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;
@Service
public class OrderEventProducer {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;

    @Autowired
    public OrderEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${spring.kafka.topic.name}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publishOrderCreated(CreateOrderRequest orderJson) {
        logger.info("Publishing order to topic: {}", topicName);

        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topicName, orderJson.toString());

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Message sent successfully to topic: {} with offset: {}",
                        topicName, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send message to topic: {}", topicName, ex);
            }
        });
    }
}