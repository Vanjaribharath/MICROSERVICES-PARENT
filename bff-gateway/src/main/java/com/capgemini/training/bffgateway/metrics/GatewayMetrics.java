package com.capgemini.training.bffgateway.metrics;

import io.micrometer.core.instrument.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Day 20 — Custom Prometheus metrics for the BFF layer. */
@Component @RequiredArgsConstructor
public class GatewayMetrics {
    private final MeterRegistry registry;
    private Counter requestsTotal;
    private Counter authFailures;
    private Timer   routingDuration;

    @PostConstruct
    void init() {
        requestsTotal  = Counter.builder("gateway.requests.total")
            .description("Total requests through BFF").tag("service","bff-gateway").register(registry);
        authFailures   = Counter.builder("gateway.auth.failures.total")
            .description("JWT auth failures").tag("service","bff-gateway").register(registry);
        routingDuration = Timer.builder("gateway.routing.duration")
            .description("Request routing duration")
            .publishPercentiles(0.50,0.95,0.99).register(registry);
    }
    public void recordRequest()     { requestsTotal.increment(); }
    public void recordAuthFailure() { authFailures.increment(); }
    public Timer.Sample startTimer() { return Timer.start(registry); }
    public void stopTimer(Timer.Sample s) { s.stop(routingDuration); }
}
