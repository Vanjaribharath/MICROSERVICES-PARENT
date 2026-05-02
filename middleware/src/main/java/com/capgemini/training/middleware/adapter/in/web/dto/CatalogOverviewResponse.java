package com.capgemini.training.middleware.adapter.in.web.dto;

import com.capgemini.training.middleware.adapter.out.feign.dto.ProductDTO;
import com.capgemini.training.middleware.domain.model.CatalogOverview;
import java.util.List;

public record CatalogOverviewResponse(
    List<ProductDTO> featuredProducts, int categoryCount, long totalProducts
) {
    public static CatalogOverviewResponse from(CatalogOverview o) {
        return new CatalogOverviewResponse(o.featuredProducts(), o.categoryCount(), o.totalProducts());
    }
}
