package com.capgemini.training.systemapi.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Day 24 — JDBC repository for saga_transactions + saga_steps tables.
 * Tracks all long-running distributed transactions.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SagaRepository {

    private final JdbcTemplate jdbc;

    public String createSaga(String sagaType, String payload,
                             String correlationId, String serviceName) {
        String sagaId = UUID.randomUUID().toString();
        try {
            jdbc.update("""
                INSERT INTO saga_transactions
                  (saga_id,saga_type,status,payload,correlation_id,service_name,started_at)
                VALUES (?::uuid,?,'STARTED',?::jsonb,?,?,NOW())
                """, sagaId, sagaType, payload, correlationId, serviceName);
        } catch (Exception ex) {
            log.error("Failed to create saga: {}", ex.getMessage());
        }
        return sagaId;
    }

    public void updateSagaStatus(String sagaId, String status, String result) {
        jdbc.update("""
            UPDATE saga_transactions
            SET status=?, result=?::jsonb, updated_at=NOW(),
                completed_at = CASE WHEN ? IN ('COMPLETED','COMPENSATED','FAILED')
                               THEN NOW() ELSE NULL END
            WHERE saga_id=?::uuid
            """, status, result, status, sagaId);
    }

    public void addSagaStep(String sagaId, String stepName, int stepOrder,
                            String inputPayload) {
        jdbc.update("""
            INSERT INTO saga_steps
              (saga_id,step_name,step_order,status,input_payload,started_at)
            VALUES (?::uuid,?,?,'EXECUTING',?::jsonb,NOW())
            """, sagaId, stepName, stepOrder, inputPayload);
    }

    public void completeSagaStep(String sagaId, String stepName, String outputPayload) {
        jdbc.update("""
            UPDATE saga_steps SET status='COMPLETED', output_payload=?::jsonb, completed_at=NOW()
            WHERE saga_id=?::uuid AND step_name=?
            """, outputPayload, sagaId, stepName);
    }

    public void failSagaStep(String sagaId, String stepName, String errorMessage) {
        jdbc.update("""
            UPDATE saga_steps SET status='FAILED', error_message=?, completed_at=NOW()
            WHERE saga_id=?::uuid AND step_name=?
            """, errorMessage, sagaId, stepName);
    }
}
