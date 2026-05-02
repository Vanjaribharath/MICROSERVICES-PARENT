package com.capgemini.training.systemapi.kafka;

import com.capgemini.training.systemapi.adapter.out.persistence.DomainEventRepository;
import com.capgemini.training.systemapi.events.ProductEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Day 23 — Kafka Producer for product domain events.
 * Every product mutation (create/update/delete) publishes an event.
 * Event is ALSO saved to domain_events table for audit + idempotency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final DomainEventRepository domainEventRepository;

    public void publishProductEvent(ProductEvent event) {
        String correlationId = MDC.get("correlationId");
        try {
            String payload = objectMapper.writeValueAsString(event);

            // Save to domain_events table FIRST (outbox pattern guarantee)
            domainEventRepository.saveEvent(
                event.eventId(), event.eventType(),
                KafkaTopicConfig.PRODUCT_EVENTS_TOPIC,
                String.valueOf(event.productId()), "Product",
                payload, "PRODUCED", correlationId, "system-api");

            // Then publish to Kafka
            CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(
                    KafkaTopicConfig.PRODUCT_EVENTS_TOPIC,
                    String.valueOf(event.productId()),  // key = productId (ensures ordering per product)
                    payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event published: type={} productId={} partition={} offset={}",
                        event.eventType(), event.productId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event: type={} productId={} error={}",
                        event.eventType(), event.productId(), ex.getMessage());
                    // Update domain_events status to FAILED
                    domainEventRepository.updateStatus(event.eventId(), "FAILED", ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("Event serialization failed: {}", e.getMessage(), e);
        }
    }
}
