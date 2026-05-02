package com.capgemini.training.systemapi.adapter.in.web.dto;

import com.capgemini.training.systemapi.application.port.in.UpdateProductCommand;
import com.capgemini.training.systemapi.domain.model.ProductStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateProductRequest(
    @Size(max = 200) String name,
    @DecimalMin("0.01") BigDecimal price,
    ProductStatus status, Long categoryId, Long supplierId
) {
    public UpdateProductCommand toCommand() {
        return new UpdateProductCommand(name, price, status, categoryId, supplierId);
    }
}
