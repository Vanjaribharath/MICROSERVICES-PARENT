package com.capgemini.training.systemapi.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Day 24 — JDBC repository for error_events table.
 * Async write so errors are never slowed by DB.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ErrorEventRepository {

    private final JdbcTemplate jdbc;

    @Async
    public void saveError(String errorType, String errorCode, String message,
                          String stackTrace, String serviceName, String endpoint,
                          String httpMethod, String correlationId, String sagaId,
                          String requestPayload) {
        try {
            jdbc.update("""
                INSERT INTO error_events
                  (error_id,error_type,error_code,message,stack_trace,
                   service_name,endpoint,http_method,correlation_id,
                   saga_id,request_payload,occurred_at)
                VALUES (?,?,?,?,?,?,?,?,?,?::uuid,?,NOW())
                """,
                UUID.randomUUID().toString(), errorType, errorCode, message,
                stackTrace, serviceName, endpoint, httpMethod, correlationId,
                sagaId, requestPayload);
        } catch (Exception ex) {
            log.error("Failed to save error event: {}", ex.getMessage());
        }
    }
}
