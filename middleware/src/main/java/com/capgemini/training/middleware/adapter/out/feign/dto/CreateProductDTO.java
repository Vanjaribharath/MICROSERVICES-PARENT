package com.capgemini.training.middleware.adapter.out.feign.dto;
import java.math.BigDecimal;
public record CreateProductDTO(String name, BigDecimal price, Long categoryId, Long supplierId) {}
