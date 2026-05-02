package com.capgemini.training.middleware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Day 11 — @EnableFeignClients for OpenFeign.
 * Day 23 — @EnableKafka for Kafka consumer.
 */
@SpringBootApplication
@EnableFeignClients
@EnableKafka
public class MiddlewareApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiddlewareApplication.class, args);
    }
}
