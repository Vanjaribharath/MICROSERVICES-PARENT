package com.capgemini.training.systemapi.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Day 23 — Domain Events observation endpoints. Trainer demo: show Kafka events in DB.
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Domain Events", description = "View Kafka event audit log — Day 23 demonstration")
public class EventController {

    private final JdbcTemplate jdbc;

    @GetMapping
    @Operation(summary = "List all domain events (Kafka audit log)")
    public List<Map<String, Object>> listEvents() {
        return jdbc.queryForList(
            "SELECT event_id,event_type,topic,aggregate_id,aggregate_type," +
            "status,correlation_id,service_name,produced_at,consumed_at,error_message " +
            "FROM domain_events ORDER BY produced_at DESC LIMIT 100");
    }

    @GetMapping("/type/{eventType}")
    @Operation(summary = "Get events by type: PRODUCT_CREATED / PRODUCT_UPDATED / PRODUCT_DELETED")
    public List<Map<String, Object>> byType(@PathVariable String eventType) {
        return jdbc.queryForList(
            "SELECT * FROM domain_events WHERE event_type=? ORDER BY produced_at DESC",
            eventType);
    }

    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Trace all events for a correlation ID")
    public List<Map<String, Object>> byCorrelation(@PathVariable String correlationId) {
        return jdbc.queryForList(
            "SELECT * FROM domain_events WHERE correlation_id=? ORDER BY produced_at",
            correlationId);
    }

    @GetMapping("/errors")
    @Operation(summary = "List all error events")
    public List<Map<String, Object>> errorEvents() {
        return jdbc.queryForList(
            "SELECT error_id,error_type,error_code,message,service_name," +
            "endpoint,correlation_id,saga_id,occurred_at,resolved " +
            "FROM error_events ORDER BY occurred_at DESC LIMIT 50");
    }
}
