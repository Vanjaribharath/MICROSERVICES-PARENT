package com.capgemini.training.systemapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/** Day 10 — Integration test with real PostgreSQL via Testcontainers. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductControllerIT {

    @Container
    static PostgreSQLContainer<?> pg =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",      pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
        r.add("spring.flyway.enabled",      () -> "true");
        r.add("management.tracing.sampling.probability", () -> "0.0");
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    @Test
    void createProduct_returns201() {
        // Seed data from V4 gives category id=1
        var body = Map.of("name","Test Widget","price",9.99,"categoryId",1);
        var resp = rest.postForEntity(
            "http://localhost:" + port + "/api/v1/products", body, Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getHeaders().getLocation()).isNotNull();
    }

    @Test
    void createProduct_returns400_whenNameBlank() {
        var body = Map.of("name","","price",9.99,"categoryId",1);
        var resp = rest.postForEntity(
            "http://localhost:" + port + "/api/v1/products", body, Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getProduct_returns404_whenNotFound() {
        var resp = rest.getForEntity(
            "http://localhost:" + port + "/api/v1/products/99999", Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void correlationId_inResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-corr-123");
        var resp = rest.exchange(
            "http://localhost:" + port + "/api/v1/products",
            HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertThat(resp.getHeaders().getFirst("X-Correlation-ID")).isEqualTo("test-corr-123");
    }

    @Test
    void healthProbe_liveness_returnsUp() {
        var resp = rest.getForEntity(
            "http://localhost:" + port + "/actuator/health/liveness", Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("status")).isEqualTo("UP");
    }
}
