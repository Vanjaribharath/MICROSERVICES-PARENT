package com.capgemini.training.middleware.domain.model;
import com.capgemini.training.middleware.adapter.out.feign.dto.ProductDTO;
import java.math.BigDecimal;
public record EnrichedProduct(ProductDTO rawProduct, BigDecimal taxAmount,
    String displayLabel, String formattedPrice) {}
