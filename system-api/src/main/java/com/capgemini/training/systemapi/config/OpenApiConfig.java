package com.capgemini.training.systemapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Day 10 — SpringDoc OpenAPI. Swagger UI at /swagger-ui.html */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
            .title("System API — Layer 3")
            .version("1.0.0")
            .description("Data Access Layer. Capgemini 30-Day Microservices Training.")
            .contact(new Contact().name("Capgemini Training").email("training@capgemini.com")));
    }
}
