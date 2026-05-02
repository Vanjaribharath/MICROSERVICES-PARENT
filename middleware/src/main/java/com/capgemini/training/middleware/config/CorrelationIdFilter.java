package com.capgemini.training.middleware.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component @Order(1) @Slf4j
public class CorrelationIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;
        String corrId = request.getHeader("X-Correlation-ID");
        if (corrId == null || corrId.isBlank()) corrId = UUID.randomUUID().toString();
        MDC.put("correlationId", corrId);
        response.setHeader("X-Correlation-ID", corrId);
        long start = System.currentTimeMillis();
        log.info(">> REQUEST [{} {}] correlationId={}", request.getMethod(), request.getRequestURI(), corrId);
        try {
            chain.doFilter(req, res);
        } finally {
            log.info("<< RESPONSE [{} {}] status={} {}ms correlationId={}",
                request.getMethod(), request.getRequestURI(), response.getStatus(),
                System.currentTimeMillis() - start, corrId);
            MDC.remove("correlationId");
        }
    }
}
