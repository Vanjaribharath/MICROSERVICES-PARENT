package com.capgemini.training.systemapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Day 3/6 — Pure domain entity. No JPA, No Spring.
 * Java 21 Record with domain validation in compact constructor.
 */
public record Product(
    Long id, String name, BigDecimal price,
    ProductStatus status, Long categoryId,
    Long supplierId, LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public Product {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price cannot be negative: " + price);
        if (name != null && name.isBlank())
            throw new IllegalArgumentException("Product name cannot be blank");
    }
    public String statusLabel() {
        return switch (status) {
            case ACTIVE   -> "Available";
            case ARCHIVED -> "Discontinued";
            case DRAFT    -> "Coming Soon";
        };
    }
}
