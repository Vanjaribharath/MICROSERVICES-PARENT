package com.capgemini.training.systemapi.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

/**
 * Day 23 — JDBC repository for domain_events table.
 * Deliberately uses JdbcTemplate (not JPA) to avoid transaction entanglement.
 * Events are written BEFORE Kafka send — outbox pattern.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class DomainEventRepository {

    private final JdbcTemplate jdbc;

    public void saveEvent(String eventId, String eventType, String topic,
                          String aggregateId, String aggregateType, String payload,
                          String status, String correlationId, String serviceName) {
        try {
            jdbc.update("""
                INSERT INTO domain_events
                  (event_id,event_type,topic,aggregate_id,aggregate_type,
                   payload,status,correlation_id,service_name,produced_at)
                VALUES (?::uuid,?,?,?,?,?::jsonb,?,?,?,NOW())
                """,
                eventId, eventType, topic, aggregateId, aggregateType,
                payload, status, correlationId, serviceName);
        } catch (Exception ex) {
            log.error("Failed to save domain event: {}", ex.getMessage());
        }
    }

    public void markConsumed(String eventId) {
        jdbc.update("UPDATE domain_events SET status='CONSUMED', consumed_at=NOW() WHERE event_id=?::uuid", eventId);
    }

    public void updateStatus(String eventId, String status, String errorMessage) {
        jdbc.update("UPDATE domain_events SET status=?, error_message=? WHERE event_id=?::uuid",
            status, errorMessage, eventId);
    }
}
