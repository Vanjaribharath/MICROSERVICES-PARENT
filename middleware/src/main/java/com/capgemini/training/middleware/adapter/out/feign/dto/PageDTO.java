package com.capgemini.training.middleware.adapter.out.feign.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record PageDTO<T>(List<T> content, int totalPages, long totalElements, int number, int size) {}
