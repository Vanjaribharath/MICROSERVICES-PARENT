package com.capgemini.training.middleware.adapter.in.web;

import com.capgemini.training.middleware.adapter.in.web.dto.*;
import com.capgemini.training.middleware.application.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Day 11/12 — Middleware catalog endpoints. Day 15 — ApiResponse envelope. */
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Middleware Layer 2 — Enrichment & Composition")
public class CatalogController {

    private final GetEnrichedProductUseCase enrichedUC;
    private final GetCatalogOverviewUseCase overviewUC;

    @GetMapping("/products/{id}")
    @Operation(summary = "Get enriched product with 18% GST calculation")
    public ApiResponse<EnrichedProductResponse> getEnriched(@PathVariable Long id) {
        return ApiResponse.of(EnrichedProductResponse.from(enrichedUC.getEnriched(id)));
    }

    @GetMapping("/overview")
    @Operation(summary = "Catalog overview — parallel fetch products + categories")
    public ApiResponse<CatalogOverviewResponse> getOverview() {
        return ApiResponse.of(CatalogOverviewResponse.from(overviewUC.getOverview()));
    }
}
