package com.capgemini.training.bffgateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Day 16 — Logs every request with method, path, duration. Adds X-Response-Time. */
@Component @Slf4j
public class RequestTimingFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start  = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path   = exchange.getRequest().getPath().value();
        String corrId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        log.info("GW >> [{} {}] correlationId={}", method, path, corrId);
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long ms     = System.currentTimeMillis() - start;
            int  status = exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value() : 0;
            exchange.getResponse().getHeaders().add("X-Response-Time", ms + "ms");
            log.info("GW << [{} {}] status={} {}ms correlationId={}", method, path, status, ms, corrId);
        }));
    }
    @Override public int getOrder() { return -1; }
}
