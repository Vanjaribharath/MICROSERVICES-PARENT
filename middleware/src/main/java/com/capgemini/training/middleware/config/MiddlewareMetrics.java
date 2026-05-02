package com.capgemini.training.middleware.config;

import io.micrometer.core.instrument.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Day 20 — Custom Prometheus metrics for middleware layer. */
@Component @RequiredArgsConstructor
public class MiddlewareMetrics {
    private final MeterRegistry registry;
    private Counter catalogRequests;
    private Counter enrichmentFallbacks;
    private Counter eventsConsumed;
    private Counter eventConsumeFailed;
    private Timer   enrichmentDuration;

    @PostConstruct void init() {
        catalogRequests      = Counter.builder("middleware.catalog.requests.total").tag("service","middleware").register(registry);
        enrichmentFallbacks  = Counter.builder("middleware.enrichment.fallbacks.total").tag("service","middleware").register(registry);
        eventsConsumed       = Counter.builder("middleware.kafka.events.consumed.total").tag("topic","product-events").register(registry);
        eventConsumeFailed   = Counter.builder("middleware.kafka.events.failed.total").tag("topic","product-events").register(registry);
        enrichmentDuration   = Timer.builder("middleware.enrichment.duration").publishPercentiles(0.50,0.95,0.99).register(registry);
    }

    public void recordCatalogRequest()    { catalogRequests.increment(); }
    public void recordFallback()          { enrichmentFallbacks.increment(); }
    public void recordEventConsumed()     { eventsConsumed.increment(); }
    public void recordEventFailed()       { eventConsumeFailed.increment(); }
    public Timer.Sample startTimer()      { return Timer.start(registry); }
    public void stopTimer(Timer.Sample s) { s.stop(enrichmentDuration); }
}
