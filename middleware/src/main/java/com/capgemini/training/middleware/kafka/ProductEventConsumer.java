package com.capgemini.training.middleware.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

/**
 * Day 23 — Kafka Consumer in Middleware layer.
 * Listens to product-events topic published by system-api.
 * Demonstrates event-driven communication between microservices.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "product-events",
        groupId = "middleware-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProductEvent(
            ConsumerRecord<String, String> record,
            Acknowledgment ack) {

        String correlationId = null;
        try {
            // Parse the event
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(record.value(), Map.class);

            correlationId = (String) event.get("correlationId");
            if (correlationId == null) correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);

            String eventType  = (String) event.get("eventType");
            Object productId  = event.get("productId");
            Object eventId    = event.get("eventId");

            log.info("EVENT RECEIVED: type={} productId={} eventId={} partition={} offset={} correlationId={}",
                eventType, productId, eventId,
                record.partition(), record.offset(), correlationId);

            // Route to handler based on event type
            switch (String.valueOf(eventType)) {
                case "PRODUCT_CREATED" -> handleProductCreated(event);
                case "PRODUCT_UPDATED" -> handleProductUpdated(event);
                case "PRODUCT_DELETED" -> handleProductDeleted(event);
                default -> log.warn("Unknown event type: {}", eventType);
            }

            // Manual acknowledge — at-least-once delivery
            ack.acknowledge();
            log.info("EVENT ACKNOWLEDGED: eventId={}", eventId);

        } catch (Exception ex) {
            log.error("Failed to process product event: partition={} offset={} error={}",
                record.partition(), record.offset(), ex.getMessage(), ex);
            // Do NOT acknowledge — will be retried
        } finally {
            MDC.remove("correlationId");
        }
    }

    private void handleProductCreated(Map<String, Object> event) {
        log.info("SAGA EVENT: ProductCreated — productId={} name={} price={}",
            event.get("productId"), event.get("productName"), event.get("price"));
        // In a real system: update read model, cache, search index, send notification
    }

    private void handleProductUpdated(Map<String, Object> event) {
        log.info("SAGA EVENT: ProductUpdated — productId={} status={}",
            event.get("productId"), event.get("status"));
    }

    private void handleProductDeleted(Map<String, Object> event) {
        log.info("SAGA EVENT: ProductDeleted — productId={}", event.get("productId"));
    }
}
