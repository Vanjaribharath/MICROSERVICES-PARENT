package com.capgemini.training.systemapi.application.port.in;

import com.capgemini.training.systemapi.domain.model.Product;
import com.capgemini.training.systemapi.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface GetProductUseCase {
    Product getById(Long id);
    Page<Product> list(Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> search(String query, Pageable pageable);
}
