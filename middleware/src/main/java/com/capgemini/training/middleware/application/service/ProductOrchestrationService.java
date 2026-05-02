package com.capgemini.training.middleware.application.service;

import com.capgemini.training.middleware.adapter.out.feign.SystemApiClient;
import com.capgemini.training.middleware.adapter.out.feign.dto.ProductDTO;
import com.capgemini.training.middleware.application.port.in.GetEnrichedProductUseCase;
import com.capgemini.training.middleware.domain.model.EnrichedProduct;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Day 11 — Calls system-api via Feign, adds tax enrichment (Day 12).
 * Day 13 — CircuitBreaker + Retry + Bulkhead for fault tolerance.
 */
@Service @RequiredArgsConstructor @Slf4j
public class ProductOrchestrationService implements GetEnrichedProductUseCase {

    private final SystemApiClient systemApiClient;
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    @Override
    @CircuitBreaker(name = "systemApi", fallbackMethod = "enrichedFallback")
    @Retry(name = "systemApi")
    @Bulkhead(name = "systemApi")
    public EnrichedProduct getEnriched(Long id) {
        log.info("Enriching product id={}", id);
        ProductDTO raw = systemApiClient.getProductById(id);
        BigDecimal tax = raw.price().multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        String label = "[" + raw.status() + "] " + raw.name().toUpperCase();
        String formatted = "₹" + String.format("%.2f", raw.price());
        log.info("Product id={} enriched, tax={}", id, tax);
        return new EnrichedProduct(raw, tax, label, formatted);
    }

    private EnrichedProduct enrichedFallback(Long id, Throwable t) {
        log.warn("Fallback for product id={}: {}", id, t.getMessage());
        ProductDTO placeholder = new ProductDTO(id, "Unavailable", BigDecimal.ZERO,
            "UNKNOWN", null, null, null, null);
        return new EnrichedProduct(placeholder, BigDecimal.ZERO, "[UNAVAILABLE]", "₹0.00");
    }
}
