package com.capgemini.training.systemapi.adapter.in.web.dto;

import com.capgemini.training.systemapi.domain.model.Product;
import com.capgemini.training.systemapi.domain.model.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Product response")
public record ProductResponse(
    Long id, String name, BigDecimal price,
    ProductStatus status, Long categoryId, Long supplierId,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.id(), p.name(), p.price(),
            p.status(), p.categoryId(), p.supplierId(),
            p.createdAt(), p.updatedAt());
    }
}
