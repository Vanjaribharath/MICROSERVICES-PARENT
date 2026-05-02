package com.capgemini.training.middleware.domain.model;
import com.capgemini.training.middleware.adapter.out.feign.dto.ProductDTO;
import java.util.List;
public record CatalogOverview(List<ProductDTO> featuredProducts,
    int categoryCount, long totalProducts) {}
