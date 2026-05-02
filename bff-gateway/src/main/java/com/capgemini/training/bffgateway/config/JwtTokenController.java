package com.capgemini.training.bffgateway.config;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Day 17 — Token endpoint for LOCAL development/testing.
 * POST /auth/token → {"username":"admin","role":"ADMIN"} → JWT
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class JwtTokenController {

    @Value("${jwt.secret:capgemini-training-secret-key-256-bits-minimum-length-here}")
    private String jwtSecret;

    @Value("${jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    @PostMapping("/token")
    public Mono<Map<String, Object>> token(@RequestBody TokenRequest req) {
        log.info("Generating token for user={} role={}", req.username(), req.role());
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] p = new byte[32];
            System.arraycopy(keyBytes, 0, p, 0, Math.min(keyBytes.length, 32));
            keyBytes = p;
        }
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
            .subject(req.username())
            .claim("roles", List.of(req.role()))
            .issuedAt(new Date(now))
            .expiration(new Date(now + expirationSeconds * 1000))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
        return Mono.just(Map.of(
            "token", token, "tokenType", "Bearer",
            "expiresIn", expirationSeconds, "username", req.username(), "role", req.role()));
    }

    public record TokenRequest(String username, String role) {}
}
