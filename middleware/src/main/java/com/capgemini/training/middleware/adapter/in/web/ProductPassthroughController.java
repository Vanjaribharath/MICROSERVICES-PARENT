package com.capgemini.training.middleware.adapter.in.web;

import com.capgemini.training.middleware.adapter.in.web.dto.ApiResponse;
import com.capgemini.training.middleware.adapter.out.feign.SystemApiClient;
import com.capgemini.training.middleware.adapter.out.feign.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;

/**
 * Day 11/12 — Middleware product pass-through.
 * BFF routes /api/v1/products/** here; middleware delegates to system-api via Feign.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products (Middleware)", description = "Product CRUD delegated to System API")
public class ProductPassthroughController {

    private final SystemApiClient systemApiClient;

    @GetMapping
    @CircuitBreaker(name = "systemApi", fallbackMethod = "listFallback")
    @Operation(summary = "List products — delegates to System API")
    public ApiResponse<PageDTO<ProductDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.of(systemApiClient.getProducts(page, size));
    }

    private ApiResponse<PageDTO<ProductDTO>> listFallback(int page, int size, Throwable t) {
        log.warn("Product list fallback: {}", t.getMessage());
        return ApiResponse.of(new PageDTO<>(Collections.emptyList(), 0, 0, page, size));
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "systemApi", fallbackMethod = "getByIdFallback")
    @Operation(summary = "Get product by ID — delegates to System API")
    public ApiResponse<ProductDTO> getById(@PathVariable Long id) {
        return ApiResponse.of(systemApiClient.getProductById(id));
    }

    private ApiResponse<ProductDTO> getByIdFallback(Long id, Throwable t) {
        log.warn("Product getById fallback id={}: {}", id, t.getMessage());
        return ApiResponse.of(new ProductDTO(id, "Unavailable", BigDecimal.ZERO,
            "UNKNOWN", null, null, null, null));
    }

    @PostMapping
    @CircuitBreaker(name = "systemApi")
    @Operation(summary = "Create product — delegates to System API")
    public ResponseEntity<ApiResponse<ProductDTO>> create(@RequestBody CreateProductRequest req) {
        if (req.name() == null || req.name().isBlank())
            throw new IllegalArgumentException("Product name must not be blank");
        if (req.price() == null || req.price().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Price must be greater than zero");
        if (req.categoryId() == null)
            throw new IllegalArgumentException("categoryId is required");
        ProductDTO created = systemApiClient.createProduct(
            new CreateProductDTO(req.name().trim(), req.price(), req.categoryId(), req.supplierId()));
        return ResponseEntity.created(URI.create("/api/v1/products/" + created.id()))
            .body(ApiResponse.of(created));
    }

    public record CreateProductRequest(
        @NotBlank String name, @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotNull Long categoryId, Long supplierId) {}
}
