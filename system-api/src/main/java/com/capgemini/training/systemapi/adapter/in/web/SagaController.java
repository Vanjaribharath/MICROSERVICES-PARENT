package com.capgemini.training.systemapi.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Day 24 — Saga observation endpoints. Trainer demo: show saga state in real-time.
 */
@RestController
@RequestMapping("/api/v1/sagas")
@RequiredArgsConstructor
@Tag(name = "Saga Monitoring", description = "View Saga state — Day 24 demonstration")
public class SagaController {

    private final JdbcTemplate jdbc;

    @GetMapping
    @Operation(summary = "List all saga transactions")
    public List<Map<String, Object>> listSagas() {
        return jdbc.queryForList(
            "SELECT saga_id,saga_type,status,current_step,correlation_id," +
            "started_at,completed_at,error_message FROM saga_transactions " +
            "ORDER BY started_at DESC LIMIT 50");
    }

    @GetMapping("/{sagaId}")
    @Operation(summary = "Get saga by ID with all steps")
    public Map<String, Object> getSaga(@PathVariable String sagaId) {
        Map<String, Object> saga = jdbc.queryForMap(
            "SELECT * FROM saga_transactions WHERE saga_id=?::uuid", sagaId);
        List<Map<String, Object>> steps = jdbc.queryForList(
            "SELECT * FROM saga_steps WHERE saga_id=?::uuid ORDER BY step_order", sagaId);
        saga.put("steps", steps);
        return saga;
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get sagas by status: STARTED/COMPLETED/FAILED/COMPENSATED")
    public List<Map<String, Object>> byStatus(@PathVariable String status) {
        return jdbc.queryForList(
            "SELECT saga_id,saga_type,status,correlation_id,started_at,error_message " +
            "FROM saga_transactions WHERE status=? ORDER BY started_at DESC", status);
    }
}
