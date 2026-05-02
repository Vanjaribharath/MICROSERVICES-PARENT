package com.capgemini.training.middleware.application.port.in;
import com.capgemini.training.middleware.domain.model.EnrichedProduct;
public interface GetEnrichedProductUseCase { EnrichedProduct getEnriched(Long id); }
