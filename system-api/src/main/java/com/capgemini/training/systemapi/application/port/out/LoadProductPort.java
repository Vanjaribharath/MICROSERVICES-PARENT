package com.capgemini.training.systemapi.application.port.out;

import com.capgemini.training.systemapi.domain.model.Product;
import com.capgemini.training.systemapi.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface LoadProductPort {
    Optional<Product> findById(Long id);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> search(String query, Pageable pageable);
}
