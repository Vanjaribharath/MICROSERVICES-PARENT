package com.capgemini.training.systemapi.adapter.out.persistence;

import com.capgemini.training.systemapi.application.port.out.*;
import com.capgemini.training.systemapi.domain.model.Product;
import com.capgemini.training.systemapi.domain.model.ProductStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Day 3/8 — Outbound adapter: implements persistence ports.
 * Bridges domain model ↔ JPA entities. Service never sees JPA.
 */
@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter
        implements SaveProductPort, LoadProductPort {

    private final ProductJpaRepository productRepo;
    private final CategoryJpaRepository categoryRepo;
    private final SupplierJpaRepository supplierRepo;

    @Override
    public Product save(Product p) {
        CategoryJpaEntity cat = categoryRepo.findById(p.categoryId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Category not found: " + p.categoryId()));
        SupplierJpaEntity sup = null;
        if (p.supplierId() != null)
            sup = supplierRepo.findById(p.supplierId()).orElse(null);

        ProductJpaEntity entity = ProductJpaEntity.builder()
            .id(p.id()).name(p.name()).price(p.price())
            .status(p.status() != null ? p.status() : ProductStatus.ACTIVE)
            .category(cat).supplier(sup).build();
        return toDomain(productRepo.save(entity));
    }

    @Override public void deleteById(Long id) { productRepo.deleteById(id); }
    @Override public boolean existsById(Long id) { return productRepo.existsById(id); }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Product> findAll(Pageable p) {
        return productRepo.findAll(p).map(this::toDomain);
    }

    @Override
    public Page<Product> findByStatus(ProductStatus s, Pageable p) {
        return productRepo.findByStatus(s, p).map(this::toDomain);
    }

    @Override
    public List<Product> findByCategoryId(Long id) {
        // FIX: use corrected method name (Spring Data traversal)
        return productRepo.findByCategory_Id(id)
            .stream().map(this::toDomain).toList();
    }

    @Override
    public Page<Product> search(String q, Pageable p) {
        return productRepo.search(q, p).map(this::toDomain);
    }

    private Product toDomain(ProductJpaEntity e) {
        return new Product(
            e.getId(), e.getName(), e.getPrice(), e.getStatus(),
            e.getCategory() != null ? e.getCategory().getId() : null,
            e.getSupplier() != null ? e.getSupplier().getId() : null,
            e.getCreatedAt(), e.getUpdatedAt());
    }
}
