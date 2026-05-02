package com.capgemini.training.systemapi.config;

import io.micrometer.core.instrument.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Day 20 — Custom Prometheus metrics.
 * Scraped at: GET /actuator/prometheus
 * RED method: Rate, Errors, Duration.
 */
@Component @RequiredArgsConstructor
public class ProductMetrics {
    private final MeterRegistry registry;
    private Counter productsCreated;
    private Counter productsUpdated;
    private Counter productsDeleted;
    private Counter sagasCompleted;
    private Counter sagasFailed;
    private Counter eventsPublished;
    private Counter eventsFailed;
    private Timer   productCreateDuration;

    @PostConstruct void init() {
        productsCreated      = Counter.builder("products.created.total").tag("service","system-api").register(registry);
        productsUpdated      = Counter.builder("products.updated.total").tag("service","system-api").register(registry);
        productsDeleted      = Counter.builder("products.deleted.total").tag("service","system-api").register(registry);
        sagasCompleted       = Counter.builder("saga.completed.total").tag("type","CreateProductSaga").register(registry);
        sagasFailed          = Counter.builder("saga.failed.total").tag("type","CreateProductSaga").register(registry);
        eventsPublished      = Counter.builder("kafka.events.published.total").tag("topic","product-events").register(registry);
        eventsFailed         = Counter.builder("kafka.events.failed.total").tag("topic","product-events").register(registry);
        productCreateDuration = Timer.builder("product.create.duration")
            .publishPercentiles(0.50,0.95,0.99).register(registry);
    }

    public void recordCreated()         { productsCreated.increment(); }
    public void recordUpdated()         { productsUpdated.increment(); }
    public void recordDeleted()         { productsDeleted.increment(); }
    public void recordSagaCompleted()   { sagasCompleted.increment(); }
    public void recordSagaFailed()      { sagasFailed.increment(); }
    public void recordEventPublished()  { eventsPublished.increment(); }
    public void recordEventFailed()     { eventsFailed.increment(); }
    public Timer.Sample startTimer()    { return Timer.start(registry); }
    public void stopTimer(Timer.Sample s) { s.stop(productCreateDuration); }
}
