package com.capgemini.training.systemapi.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Centralized log viewer — queries application_logs from ALL 3 services.
 *
 * Every service (system-api, middleware, bff-gateway) writes to the same
 * application_logs table in capgemini_db with their own service_name.
 *
 * Query by:
 *   - correlationId → trace a single request across all 3 layers
 *   - traceId       → OpenTelemetry distributed trace
 *   - service        → filter logs for one service
 *   - level          → ERROR, WARN, INFO, DEBUG
 *   - last N minutes → recent activity
 */
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Centralized Logs", description = "Query logs from ALL microservices (system-api, middleware, bff-gateway)")
public class LogController {

    private final JdbcTemplate jdbc;

    /**
     * GET /api/v1/logs — list recent logs across all services.
     * Filters: correlationId, traceId, spanId, service, level, lastMinutes, limit
     */
    @GetMapping
    @Operation(summary = "Query logs across all 3 services with filters")
    public Map<String, Object> getLogs(
            @Parameter(description = "Filter by correlation ID") @RequestParam(required = false) String correlationId,
            @Parameter(description = "Filter by OTel trace ID") @RequestParam(required = false) String traceId,
            @Parameter(description = "Filter by OTel span ID") @RequestParam(required = false) String spanId,
            @Parameter(description = "Filter by service: system-api, middleware, bff-gateway") @RequestParam(required = false) String service,
            @Parameter(description = "Filter by level: ERROR, WARN, INFO, DEBUG") @RequestParam(required = false) String level,
            @Parameter(description = "Last N minutes (default: 60)") @RequestParam(defaultValue = "60") int lastMinutes,
            @Parameter(description = "Max results (default: 100)") @RequestParam(defaultValue = "100") int limit) {

        StringBuilder sql = new StringBuilder("""
            SELECT id, timestamp, level, service_name, logger_name, message,
                   correlation_id, trace_id, span_id, thread_name, exception_detail
            FROM application_logs
            WHERE timestamp >= ?
            """);
        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(LocalDateTime.now().minusMinutes(lastMinutes)));

        if (correlationId != null && !correlationId.isBlank()) {
            sql.append(" AND correlation_id = ?");
            params.add(correlationId.trim());
        }
        if (traceId != null && !traceId.isBlank()) {
            sql.append(" AND trace_id = ?");
            params.add(traceId.trim());
        }
        if (spanId != null && !spanId.isBlank()) {
            sql.append(" AND span_id = ?");
            params.add(spanId.trim());
        }
        if (service != null && !service.isBlank()) {
            sql.append(" AND service_name = ?");
            params.add(service.trim());
        }
        if (level != null && !level.isBlank()) {
            sql.append(" AND level = ?");
            params.add(level.trim().toUpperCase());
        }

        sql.append(" ORDER BY timestamp DESC LIMIT ?");
        params.add(Math.min(limit, 500));

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());

        // Summary stats
        Long totalAll = jdbc.queryForObject("SELECT COUNT(*) FROM application_logs", Long.class);
        List<Map<String, Object>> perService = jdbc.queryForList("""
            SELECT service_name, COUNT(*) as count
            FROM application_logs
            WHERE timestamp >= ?
            GROUP BY service_name ORDER BY service_name
            """, Timestamp.valueOf(LocalDateTime.now().minusMinutes(lastMinutes)));

        return Map.of(
            "logs", rows,
            "count", rows.size(),
            "totalLogsInDb", totalAll,
            "perService", perService,
            "filters", Map.of(
                "correlationId", Objects.toString(correlationId, ""),
                "traceId", Objects.toString(traceId, ""),
                "service", Objects.toString(service, "ALL"),
                "level", Objects.toString(level, "ALL"),
                "lastMinutes", lastMinutes
            )
        );
    }

    /**
     * GET /api/v1/logs/trace/{correlationId} — trace a request across all services.
     * Returns logs from bff-gateway → middleware → system-api for one correlationId.
     */
    @GetMapping("/trace/{correlationId}")
    @Operation(summary = "Trace a single request across all 3 layers by correlation ID")
    public Map<String, Object> traceByCorrelationId(@PathVariable String correlationId) {
        List<Map<String, Object>> logs = jdbc.queryForList("""
            SELECT id, timestamp, level, service_name, logger_name, message,
                   correlation_id, trace_id, span_id, thread_name, exception_detail
            FROM application_logs
            WHERE correlation_id = ?
            ORDER BY timestamp ASC
            """, correlationId.trim());

        // Group by service for easy visualization
        Map<String, List<Map<String, Object>>> byService = new LinkedHashMap<>();
        byService.put("bff-gateway", new ArrayList<>());
        byService.put("middleware", new ArrayList<>());
        byService.put("system-api", new ArrayList<>());
        for (Map<String, Object> row : logs) {
            String svc = String.valueOf(row.get("service_name"));
            byService.computeIfAbsent(svc, k -> new ArrayList<>()).add(row);
        }

        return Map.of(
            "correlationId", correlationId,
            "totalLogs", logs.size(),
            "timeline", logs,
            "byService", byService
        );
    }

    /**
     * GET /api/v1/logs/services — summary of log activity per service.
     */
    @GetMapping("/services")
    @Operation(summary = "Log count summary per service and level")
    public List<Map<String, Object>> serviceSummary(
            @RequestParam(defaultValue = "60") int lastMinutes) {
        return jdbc.queryForList("""
            SELECT service_name, level, COUNT(*) as count,
                   MIN(timestamp) as earliest, MAX(timestamp) as latest
            FROM application_logs
            WHERE timestamp >= ?
            GROUP BY service_name, level
            ORDER BY service_name, level
            """, Timestamp.valueOf(LocalDateTime.now().minusMinutes(lastMinutes)));
    }

    /**
     * GET /api/v1/logs/errors — recent errors across all services.
     */
    @GetMapping("/errors")
    @Operation(summary = "Recent ERROR logs across all services")
    public List<Map<String, Object>> recentErrors(
            @RequestParam(defaultValue = "60") int lastMinutes,
            @RequestParam(defaultValue = "50") int limit) {
        return jdbc.queryForList("""
            SELECT id, timestamp, service_name, logger_name, message,
                   correlation_id, trace_id, span_id, exception_detail
            FROM application_logs
            WHERE level = 'ERROR' AND timestamp >= ?
            ORDER BY timestamp DESC
            LIMIT ?
            """, Timestamp.valueOf(LocalDateTime.now().minusMinutes(lastMinutes)),
            Math.min(limit, 200));
    }
}
