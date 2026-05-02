package com.capgemini.training.systemapi.config;

import com.capgemini.training.systemapi.adapter.out.persistence.ErrorEventRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Day 9  — RFC 7807 ProblemDetail.
 * Day 12 — correlationId in every error.
 * Day 24 — Every error saved asynchronously to error_events table with
 *           correlationId, traceId, spanId, endpoint, HTTP method, payload.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorEventRepository errorRepo;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex,
                                          HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(400);
        pd.setType(URI.create("/errors/validation"));
        pd.setTitle("Validation Failed");
        List<Map<String,String>> errors = ex.getFieldErrors().stream()
            .map(e -> Map.of("field", e.getField(),
                "message", e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid"))
            .toList();
        pd.setProperty("errors", errors);
        enrichProblemDetail(pd);
        saveError("ValidationException", "400", "Validation failed: " + errors, null,
                  req, null);
        return pd;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(404);
        pd.setType(URI.create("/errors/not-found"));
        pd.setTitle("Resource Not Found");
        pd.setDetail(ex.getMessage());
        enrichProblemDetail(pd);
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(400);
        pd.setType(URI.create("/errors/invalid-argument"));
        pd.setTitle("Invalid Argument");
        pd.setDetail(ex.getMessage());
        enrichProblemDetail(pd);
        saveError("IllegalArgumentException", "400", ex.getMessage(),
                  stackTrace(ex), req, null);
        return pd;
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ProblemDetail handleInvalidDataAccess(InvalidDataAccessApiUsageException ex,
                                                  HttpServletRequest req) {
        String msg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        ProblemDetail pd = ProblemDetail.forStatus(400);
        pd.setType(URI.create("/errors/invalid-request"));
        pd.setTitle("Invalid Request");
        pd.setDetail("Invalid parameter: " + msg);
        enrichProblemDetail(pd);
        saveError("InvalidDataAccessApiUsageException","400", msg, stackTrace(ex), req, null);
        return pd;
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccess(DataAccessException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(500);
        pd.setTitle("Database Error");
        pd.setDetail("A database error occurred");
        enrichProblemDetail(pd);
        log.error("DB error [correlationId={}]", MDC.get("correlationId"), ex);
        saveError("DataAccessException","500", ex.getMessage(), stackTrace(ex), req, null);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformed(HttpMessageNotReadableException ex,
                                          HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(400);
        pd.setTitle("Malformed Request Body");
        pd.setDetail("Request body is missing or invalid JSON");
        enrichProblemDetail(pd);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(500);
        pd.setType(URI.create("/errors/internal"));
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred");
        enrichProblemDetail(pd);
        log.error("Unhandled exception [correlationId={}] [traceId={}]",
                  MDC.get("correlationId"), MDC.get("traceId"), ex);
        saveError(ex.getClass().getSimpleName(), "500", ex.getMessage(),
                  stackTrace(ex), req, null);
        return pd;
    }

    private void enrichProblemDetail(ProblemDetail pd) {
        pd.setProperty("correlationId", MDC.get("correlationId"));
        pd.setProperty("traceId",       MDC.get("traceId"));
        pd.setProperty("spanId",        MDC.get("spanId"));
    }

    private void saveError(String type, String code, String message, String stackTrace,
                            HttpServletRequest req, String sagaId) {
        try {
            errorRepo.saveError(type, code, message, stackTrace, "system-api",
                req != null ? req.getRequestURI()  : null,
                req != null ? req.getMethod()       : null,
                MDC.get("correlationId"), sagaId, null);
        } catch (Exception ignored) { /* never let error logging crash the response */ }
    }

    private String stackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String s = sw.toString();
        return s.length() > 2000 ? s.substring(0, 2000) + "..." : s;
    }
}
