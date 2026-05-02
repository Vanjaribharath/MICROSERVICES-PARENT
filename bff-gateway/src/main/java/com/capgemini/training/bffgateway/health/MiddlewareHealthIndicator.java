package com.capgemini.training.bffgateway.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Day 22 — Reactive HealthIndicator: BFF checks middleware is reachable.
 *
 * FIX: onErrorResume lambda gives Throwable, not Exception.
 * Health.down(Throwable) does NOT exist — Health.down() takes Exception.
 * Solution: use Health.down().withDetail("error", msg).build() — no type passed.
 */
@Component("middleware")
@RequiredArgsConstructor
@Slf4j
public class MiddlewareHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient.Builder webClientBuilder;

    @Value("${middleware.url:http://localhost:8082}")
    private String middlewareUrl;

    @Override
    public Mono<Health> health() {
        return webClientBuilder
            .baseUrl(middlewareUrl)
            .build()
            .get()
            .uri("/actuator/health/liveness")
            .retrieve()
            .toBodilessEntity()
            .map(response -> Health.up()
                .withDetail("service", "middleware")
                .withDetail("url", middlewareUrl)
                .withDetail("status", "reachable")
                .build())
            .onErrorResume(throwable -> {
                // CORRECT: Health.down() with no argument, details added manually.
                // Cannot use Health.down(throwable) — API only accepts Exception.
                log.warn("Middleware health check failed: {}", throwable.getMessage());
                return Mono.just(
                    Health.down()
                        .withDetail("service", "middleware")
                        .withDetail("url", middlewareUrl)
                        .withDetail("error",
                            throwable.getMessage() != null
                                ? throwable.getMessage()
                                : throwable.getClass().getSimpleName())
                        .build()
                );
            });
    }
}
