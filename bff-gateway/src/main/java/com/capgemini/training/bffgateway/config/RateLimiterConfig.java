package com.capgemini.training.bffgateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Day 18 — Redis-backed rate limiting.
 * Disabled by default (local dev has no Redis).
 * Enable: redis.rate-limiter.enabled=true + Redis running on localhost:6379
 */
@Configuration
@ConditionalOnProperty(
    prefix = "redis.rate-limiter",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
@ConditionalOnClass(name = "org.springframework.data.redis.core.ReactiveRedisTemplate")
public class RateLimiterConfig {

    /** Token bucket: 10 requests/sec sustained, burst up to 20. */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    /** Rate limit per client IP address. */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String fwd = exchange.getRequest().getHeaders()
                .getFirst("X-Forwarded-For");
            if (fwd != null && !fwd.isBlank())
                return Mono.just(fwd.split(",")[0].trim());
            return Mono.justOrEmpty(
                exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getHostString()
                    : "unknown");
        };
    }
}
