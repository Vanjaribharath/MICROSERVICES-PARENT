package com.capgemini.training.middleware.adapter.out.feign.dto;
import java.time.LocalDateTime;
public record CategoryDTO(Long id, String name, String description,
    LocalDateTime createdAt, LocalDateTime updatedAt) {}
