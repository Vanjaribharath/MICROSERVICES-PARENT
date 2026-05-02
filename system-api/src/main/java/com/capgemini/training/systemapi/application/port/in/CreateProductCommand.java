package com.capgemini.training.systemapi.application.port.in;

import java.math.BigDecimal;

public record CreateProductCommand(
    String name, BigDecimal price, Long categoryId, Long supplierId
) {}
