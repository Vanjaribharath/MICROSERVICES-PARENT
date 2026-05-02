package com.capgemini.training.middleware.adapter.in.web.dto;

import com.capgemini.training.middleware.domain.model.EnrichedProduct;
import java.math.BigDecimal;

/** Day 15 — Response DTO from middleware enrichment. */
public record EnrichedProductResponse(
    Long id, String name, BigDecimal price, String status,
    BigDecimal taxAmount, String displayLabel, String formattedPrice
) {
    public static EnrichedProductResponse from(EnrichedProduct ep) {
        var p = ep.rawProduct();
        return new EnrichedProductResponse(p.id(), p.name(), p.price(), p.status(),
            ep.taxAmount(), ep.displayLabel(), ep.formattedPrice());
    }
}
