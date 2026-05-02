package com.capgemini.training.middleware.adapter.out.feign.dto;
import java.math.BigDecimal; import java.time.LocalDateTime;
public record ProductDTO(Long id, String name, BigDecimal price, String status,
    Long categoryId, Long supplierId, LocalDateTime createdAt, LocalDateTime updatedAt) {}
