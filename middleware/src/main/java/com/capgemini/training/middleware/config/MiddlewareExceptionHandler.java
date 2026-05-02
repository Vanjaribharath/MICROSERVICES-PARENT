package com.capgemini.training.middleware.config;

import feign.FeignException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.net.URI;

/** Day 14 — RFC 7807 exception translation for middleware. */
@RestControllerAdvice @Slf4j
public class MiddlewareExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegal(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(400);
        pd.setType(URI.create("/errors/invalid-request")); pd.setTitle("Invalid Request");
        pd.setDetail(ex.getMessage()); pd.setProperty("correlationId", MDC.get("correlationId"));
        return pd;
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeign(FeignException ex) {
        int mapped = switch (ex.status()) { case 400 -> 400; case 404 -> 404; default -> 502; };
        ProblemDetail pd = ProblemDetail.forStatus(mapped);
        pd.setType(URI.create("/errors/downstream")); pd.setTitle("Downstream Error");
        pd.setDetail("System API: HTTP " + ex.status());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        pd.setProperty("downstreamStatus", ex.status());
        log.error("Feign error ({}): {}", ex.status(), ex.getMessage());
        return pd;
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ProblemDetail handleCircuitOpen(CallNotPermittedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(503);
        pd.setType(URI.create("/errors/circuit-open")); pd.setTitle("Service Unavailable");
        pd.setDetail("Circuit OPEN for: " + ex.getCausingCircuitBreakerName() + ". Retry in 30s.");
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.warn("Circuit OPEN: {}", ex.getCausingCircuitBreakerName());
        return pd;
    }

    @ExceptionHandler(BulkheadFullException.class)
    public ProblemDetail handleBulkhead(BulkheadFullException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(429);
        pd.setTitle("Too Many Concurrent Requests");
        pd.setProperty("correlationId", MDC.get("correlationId")); return pd;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(404); pd.setTitle("Not Found");
        pd.setDetail(ex.getMessage()); pd.setProperty("correlationId", MDC.get("correlationId"));
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(500);
        pd.setTitle("Internal Server Error"); pd.setDetail("Unexpected error in middleware");
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.error("Unhandled [correlationId={}]", MDC.get("correlationId"), ex); return pd;
    }
}
