package com.capgemini.training.middleware.adapter.in.web.dto;

import org.slf4j.MDC;
import java.time.Instant;

/** Day 15 — Envelope wrapping data with correlationId + timestamp metadata. */
public record ApiResponse<T>(T data, ResponseMetadata metadata) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, ResponseMetadata.now());
    }
    public record ResponseMetadata(String correlationId, Instant timestamp, String service) {
        public static ResponseMetadata now() {
            return new ResponseMetadata(MDC.get("correlationId"), Instant.now(), "middleware");
        }
    }
}
