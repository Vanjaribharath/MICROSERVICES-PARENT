package com.capgemini.training.systemapi.domain.model;
import java.time.LocalDateTime;
public record Supplier(Long id, String name, String contactEmail, LocalDateTime createdAt, LocalDateTime updatedAt) {}
