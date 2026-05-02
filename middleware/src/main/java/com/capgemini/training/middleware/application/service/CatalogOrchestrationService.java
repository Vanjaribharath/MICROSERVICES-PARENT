package com.capgemini.training.middleware.application.service;

import com.capgemini.training.middleware.adapter.out.feign.SystemApiClient;
import com.capgemini.training.middleware.adapter.out.feign.dto.CategoryDTO;
import com.capgemini.training.middleware.adapter.out.feign.dto.ProductDTO;
import com.capgemini.training.middleware.application.port.in.GetCatalogOverviewUseCase;
import com.capgemini.training.middleware.domain.model.CatalogOverview;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Day 12 — Parallel composition with CompletableFuture.allOf().
 * Day 13 — CircuitBreaker fallback.
 */
@Service @RequiredArgsConstructor @Slf4j
public class CatalogOrchestrationService implements GetCatalogOverviewUseCase {

    private final SystemApiClient systemApiClient;

    @Override
    @CircuitBreaker(name = "systemApi", fallbackMethod = "overviewFallback")
    public CatalogOverview getOverview() {
        log.info("Composing catalog overview (parallel fetch)");
        CompletableFuture<List<ProductDTO>> productsFuture =
            supplyAsync(() -> systemApiClient.getProducts(0, 10).content());
        CompletableFuture<List<CategoryDTO>> categoriesFuture =
            supplyAsync(systemApiClient::getCategories);
        CompletableFuture.allOf(productsFuture, categoriesFuture).join();
        List<ProductDTO> products = productsFuture.join();
        List<CategoryDTO> categories = categoriesFuture.join();
        log.info("Catalog composed: {} products, {} categories", products.size(), categories.size());
        return new CatalogOverview(products, categories.size(), products.size());
    }

    private CatalogOverview overviewFallback(Throwable t) {
        log.warn("Catalog overview fallback: {}", t.getMessage());
        return new CatalogOverview(Collections.emptyList(), 0, 0);
    }
}
