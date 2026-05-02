package com.capgemini.training.systemapi.application.port.in;

import com.capgemini.training.systemapi.domain.model.ProductStatus;
import java.math.BigDecimal;

public record UpdateProductCommand(
    String name, BigDecimal price, ProductStatus status, Long categoryId, Long supplierId
) {}
