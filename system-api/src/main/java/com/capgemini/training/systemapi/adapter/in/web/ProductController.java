package com.capgemini.training.systemapi.adapter.in.web;

import com.capgemini.training.systemapi.adapter.in.web.dto.*;
import com.capgemini.training.systemapi.application.port.in.*;
import com.capgemini.training.systemapi.domain.model.ProductStatus;
import com.capgemini.training.systemapi.saga.CreateProductSaga;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * Day 8  — CRUD REST endpoints.
 * Day 24 — POST /products now runs through CreateProductSaga (Kafka + DB + event).
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "System API Layer 3 — Product Management")
public class ProductController {

    private final CreateProductUseCase createUC;
    private final GetProductUseCase    getUC;
    private final CreateProductSaga    createSaga;   // Day 24

    // Day 24 — Create flows through Saga (validate → persist → publish event)
    @PostMapping
    @Operation(summary = "Create product (runs Saga: persist → Kafka event)")
    @ApiResponse(responseCode = "201", description = "Product created + event published")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
        var result = createSaga.execute(req.toCommand());
        if (!result.success()) {
            throw new RuntimeException("Saga failed: " + result.errorMessage());
        }
        return ResponseEntity
            .created(URI.create("/api/v1/products/" + result.product().id()))
            .header("X-Saga-Id", result.sagaId())
            .body(ProductResponse.from(result.product()));
    }

    @GetMapping
    @Operation(summary = "List products (paginated)")
    public Page<ProductResponse> list(Pageable pageable) {
        return getUC.list(pageable).map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ProductResponse getById(@PathVariable Long id) {
        return ProductResponse.from(getUC.getById(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get products by status")
    public Page<ProductResponse> byStatus(@PathVariable ProductStatus status, Pageable pageable) {
        return getUC.findByStatus(status, pageable).map(ProductResponse::from);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public List<ProductResponse> byCategory(@PathVariable Long categoryId) {
        return getUC.findByCategoryId(categoryId).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    public Page<ProductResponse> search(@RequestParam String q, Pageable pageable) {
        return getUC.search(q, pageable).map(ProductResponse::from);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest req) {
        return ProductResponse.from(createUC.update(id, req.toCommand()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product")
    public void delete(@PathVariable Long id) { createUC.delete(id); }
}
