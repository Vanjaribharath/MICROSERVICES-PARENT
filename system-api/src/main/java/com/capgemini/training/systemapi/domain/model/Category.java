package com.capgemini.training.systemapi.domain.model;
import java.time.LocalDateTime;
public record Category(Long id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {}
