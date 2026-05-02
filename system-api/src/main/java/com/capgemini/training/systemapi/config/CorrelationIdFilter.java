package com.capgemini.training.systemapi.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

/**
 * Day 12 — X-Correlation-ID propagation.
 * Day 19 — MDC populated with correlationId + traceId + spanId for every log line.
 * Day 21 — traceId/spanId injected by Micrometer Tracing into MDC automatically.
 *           This filter adds correlationId and echoes traceId/spanId headers.
 *
 * MDC keys available in every log line:
 *   correlationId — our business correlation ID (from X-Correlation-ID header)
 *   traceId       — OTel W3C trace ID (injected by Micrometer Tracing)
 *   spanId        — OTel span ID (injected by Micrometer Tracing)
 *
 * All three are written to application_logs table AND returned in response headers.
 */
@Component @Order(1) @Slf4j
public class CorrelationIdFilter implements Filter {

    public static final String CORR_HEADER  = "X-Correlation-ID";
    public static final String TRACE_HEADER = "X-Trace-ID";
    public static final String SPAN_HEADER  = "X-Span-ID";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Correlation ID (business-level tracing)
        String corrId = request.getHeader(CORR_HEADER);
        if (corrId == null || corrId.isBlank()) corrId = UUID.randomUUID().toString();
        MDC.put("correlationId", corrId);
        response.setHeader(CORR_HEADER, corrId);

        long start = System.currentTimeMillis();
        // Log BEFORE chain — MDC already has correlationId, Micrometer will add traceId/spanId
        log.info(">> REQUEST [{} {}] correlationId={}", request.getMethod(), request.getRequestURI(), corrId);

        try {
            chain.doFilter(req, res);
        } finally {
            long ms = System.currentTimeMillis() - start;
            // Echo traceId/spanId back in response headers (set by Micrometer into MDC)
            String traceId = MDC.get("traceId");
            String spanId  = MDC.get("spanId");
            if (traceId != null) response.setHeader(TRACE_HEADER, traceId);
            if (spanId  != null) response.setHeader(SPAN_HEADER,  spanId);

            log.info("<< RESPONSE [{} {}] status={} {}ms correlationId={} traceId={} spanId={}",
                request.getMethod(), request.getRequestURI(),
                response.getStatus(), ms, corrId, traceId, spanId);

            MDC.remove("correlationId");
            // Note: traceId/spanId are managed by Micrometer — do NOT remove them here
        }
    }
}
