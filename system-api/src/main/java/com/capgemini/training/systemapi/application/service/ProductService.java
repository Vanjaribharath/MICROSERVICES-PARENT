package com.capgemini.training.systemapi.application.service;

import com.capgemini.training.systemapi.application.port.in.*;
import com.capgemini.training.systemapi.application.port.out.*;
import com.capgemini.training.systemapi.domain.model.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Day 8 — Application service implementing inbound ports.
 * Calls outbound ports (never JPA directly).
 * Day 9 — Domain invariants enforced here.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService
        implements CreateProductUseCase, GetProductUseCase {

    private final SaveProductPort savePort;
    private final LoadProductPort loadPort;

    @Override
    public Product create(CreateProductCommand cmd) {
        log.info("Creating product: name={} categoryId={}", cmd.name(), cmd.categoryId());
        Product product = new Product(null, cmd.name().trim(), cmd.price(),
            ProductStatus.ACTIVE, cmd.categoryId(), cmd.supplierId(),
            LocalDateTime.now(), LocalDateTime.now());
        Product saved = savePort.save(product);
        log.info("Product created: id={}", saved.id());
        return saved;
    }

    @Override
    public Product update(Long id, UpdateProductCommand cmd) {
        Product existing = loadPort.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        Product updated = new Product(
            existing.id(),
            cmd.name() != null ? cmd.name().trim() : existing.name(),
            cmd.price() != null ? cmd.price() : existing.price(),
            cmd.status() != null ? cmd.status() : existing.status(),
            cmd.categoryId() != null ? cmd.categoryId() : existing.categoryId(),
            cmd.supplierId() != null ? cmd.supplierId() : existing.supplierId(),
            existing.createdAt(), LocalDateTime.now());
        return savePort.save(updated);
    }

    @Override
    public void delete(Long id) {
        if (!savePort.existsById(id))
            throw new EntityNotFoundException("Product not found: " + id);
        savePort.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return loadPort.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> list(Pageable pageable) {
        return loadPort.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByStatus(ProductStatus status, Pageable pageable) {
        return loadPort.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategoryId(Long categoryId) {
        return loadPort.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(String query, Pageable pageable) {
        return loadPort.search(query, pageable);
    }
}
