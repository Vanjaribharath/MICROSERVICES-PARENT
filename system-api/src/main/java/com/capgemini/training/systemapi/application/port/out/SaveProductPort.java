package com.capgemini.training.systemapi.application.port.out;

import com.capgemini.training.systemapi.domain.model.Product;

public interface SaveProductPort {
    Product save(Product product);
    void deleteById(Long id);
    boolean existsById(Long id);
}
