package com.capgemini.training.middleware.health;

import com.capgemini.training.middleware.adapter.out.feign.SystemApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;

/** Day 22 — Checks system-api reachability via Feign. */
@Component("systemApi")
@RequiredArgsConstructor @Slf4j
public class SystemApiHealthIndicator implements HealthIndicator {
    private final SystemApiClient client;
    @Override
    public Health health() {
        try {
            client.getCategories();
            return Health.up().withDetail("service", "system-api").withDetail("status", "reachable").build();
        } catch (Exception ex) {
            log.warn("system-api health check failed: {}", ex.getMessage());
            return Health.down(ex).withDetail("service", "system-api").withDetail("error", ex.getMessage()).build();
        }
    }
}
