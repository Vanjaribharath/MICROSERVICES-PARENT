package com.capgemini.training.middleware.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Day 14 — WireMock integration test stubs system-api. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MiddlewareWireMockIT {

    static WireMockServer wireMock;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        WireMock.configureFor(wireMock.port());
        r.add("system-api.url", () -> "http://localhost:" + wireMock.port());
        r.add("management.tracing.sampling.probability", () -> "0.0");
    }

    @AfterAll static void stop() { if (wireMock != null) wireMock.stop(); }
    @BeforeEach void reset() { wireMock.resetAll(); }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    @Test
    void catalogOverview_returns200_whenSystemApiUp() {
        wireMock.stubFor(get(urlPathMatching("/api/v1/products.*"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type","application/json")
                .withBody("{\"content\":[{\"id\":1,\"name\":\"Widget\",\"price\":29.99,\"status\":\"ACTIVE\",\"categoryId\":1,\"supplierId\":null,\"createdAt\":null,\"updatedAt\":null}],\"totalPages\":1,\"totalElements\":1,\"number\":0,\"size\":20}")));
        wireMock.stubFor(get(urlEqualTo("/api/v1/categories"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type","application/json")
                .withBody("[{\"id\":1,\"name\":\"Electronics\",\"description\":\"Gadgets\",\"createdAt\":null,\"updatedAt\":null}]")));

        var resp = rest.getForEntity("http://localhost:" + port + "/api/v1/catalog/overview", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("featuredProducts");
    }

    @Test
    void correlationId_propagated_inResponse() {
        wireMock.stubFor(get(urlEqualTo("/api/v1/categories")).willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json").withBody("[]")));
        wireMock.stubFor(get(urlPathMatching("/api/v1/products.*")).willReturn(aResponse().withStatus(200).withHeader("Content-Type","application/json").withBody("{\"content\":[],\"totalPages\":0,\"totalElements\":0,\"number\":0,\"size\":20}")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", "test-wiremock-123");
        var resp = rest.exchange("http://localhost:" + port + "/api/v1/catalog/overview",
            HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getHeaders().getFirst("X-Correlation-ID")).isEqualTo("test-wiremock-123");
    }
}
