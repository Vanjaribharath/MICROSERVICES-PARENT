package com.capgemini.training.systemapi.application.port.in;

import com.capgemini.training.systemapi.domain.model.Product;

public interface CreateProductUseCase {
    Product create(CreateProductCommand cmd);
    Product update(Long id, UpdateProductCommand cmd);
    void delete(Long id);
}
