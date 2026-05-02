package com.capgemini.training.bffgateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Day 12/16 — Adds X-Correlation-ID to every routed request. Runs first (order=-2).
 * Also puts correlationId into MDC so DatabaseLogAppender can capture it.
 */
@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {
    private static final String HEADER = "X-Correlation-ID";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String corrId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (corrId == null || corrId.isBlank()) corrId = UUID.randomUUID().toString();
        final String id = corrId;
        MDC.put("correlationId", id);
        exchange.getResponse().getHeaders().add(HEADER, id);
        var req = exchange.getRequest().mutate().header(HEADER, id).build();
        return chain.filter(exchange.mutate().request(req).build())
            .doFinally(signal -> MDC.remove("correlationId"));
    }
    @Override public int getOrder() { return -2; }
}
