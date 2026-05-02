package com.capgemini.training.bffgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Day 16 — Reactive Spring Cloud Gateway. Do NOT add @EnableWebMvc. */
@SpringBootApplication
public class BffGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(BffGatewayApplication.class, args);
    }
}
