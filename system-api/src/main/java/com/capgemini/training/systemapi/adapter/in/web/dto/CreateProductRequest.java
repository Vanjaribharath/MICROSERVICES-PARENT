package com.capgemini.training.systemapi.adapter.in.web.dto;

import com.capgemini.training.systemapi.application.port.in.CreateProductCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Day 8/9 — Inbound DTO with Bean Validation. */
@Schema(description = "Create product request")
public record CreateProductRequest(
    @NotBlank @Size(max = 200) @Schema(example = "Widget Pro") String name,
    @NotNull @DecimalMin("0.01") @Schema(example = "29.99") BigDecimal price,
    @NotNull @Schema(example = "1") Long categoryId,
    @Schema(example = "1") Long supplierId
) {
    public CreateProductCommand toCommand() {
        return new CreateProductCommand(name.trim(), price, categoryId, supplierId);
    }
}
