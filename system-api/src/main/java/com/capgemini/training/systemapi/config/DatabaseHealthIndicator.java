package com.capgemini.training.systemapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Day 22 — Custom HealthIndicator. Exposed at /actuator/health/readiness */
@Component("database")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up()
                .withDetail("database", "PostgreSQL reachable")
                .withDetail("ping", result)
                .build();
        } catch (Exception ex) {
            return Health.down(ex)
                .withDetail("database", "PostgreSQL unreachable")
                .withDetail("error", ex.getMessage())
                .build();
        }
    }
}
