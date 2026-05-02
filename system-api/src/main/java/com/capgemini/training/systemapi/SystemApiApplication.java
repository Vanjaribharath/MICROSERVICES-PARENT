package com.capgemini.training.systemapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Day 2  — Spring Boot 3.2 entry point.
 * Day 6  — @EnableJpaAuditing for @CreatedDate/@LastModifiedDate.
 * Day 23 — @EnableKafka for Kafka producer/consumer.
 * Day 24 — @EnableAsync for async error event writes.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableKafka
public class SystemApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApiApplication.class, args);
    }
}
