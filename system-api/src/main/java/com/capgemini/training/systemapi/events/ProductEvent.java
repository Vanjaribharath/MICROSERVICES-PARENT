package com.capgemini.training.systemapi.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Day 23 — Domain event published to Kafka when a product changes.
 * Immutable record — serialized to JSON for Kafka message value.
 */
public record ProductEvent(
    String eventId,
    String eventType,       // PRODUCT_CREATED / PRODUCT_UPDATED / PRODUCT_DELETED
    Long   productId,
    String productName,
    BigDecimal price,
    String status,
    Long   categoryId,
    String correlationId,
    String serviceName,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime occurredAt
) {
    // Static factory methods for each event type
    public static ProductEvent created(Long id, String name, BigDecimal price,
                                       Long catId, String corrId) {
        return new ProductEvent(
            java.util.UUID.randomUUID().toString(),
            "PRODUCT_CREATED", id, name, price, "ACTIVE", catId,
            corrId, "system-api", LocalDateTime.now());
    }

    public static ProductEvent updated(Long id, String name, BigDecimal price,
                                       String status, Long catId, String corrId) {
        return new ProductEvent(
            java.util.UUID.randomUUID().toString(),
            "PRODUCT_UPDATED", id, name, price, status, catId,
            corrId, "system-api", LocalDateTime.now());
    }

    public static ProductEvent deleted(Long id, String corrId) {
        return new ProductEvent(
            java.util.UUID.randomUUID().toString(),
            "PRODUCT_DELETED", id, null, null, "DELETED", null,
            corrId, "system-api", LocalDateTime.now());
    }
}
