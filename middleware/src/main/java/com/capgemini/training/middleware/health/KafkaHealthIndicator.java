package com.capgemini.training.middleware.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Day 23 — Kafka health indicator. Checks Kafka broker reachability.
 * Included in: GET /actuator/health/readiness
 */
@Component("kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaHealthIndicator implements HealthIndicator {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Override
    public Health health() {
        try (AdminClient client = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                       AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000))) {

            Set<String> topics = client.listTopics()
                .names().get(3, TimeUnit.SECONDS);

            return Health.up()
                .withDetail("broker", bootstrapServers)
                .withDetail("topics", topics.size())
                .withDetail("topicList", topics)
                .build();
        } catch (Exception ex) {
            log.warn("Kafka health check failed: {}", ex.getMessage());
            return Health.down()
                .withDetail("broker", bootstrapServers)
                .withDetail("error",  ex.getMessage())
                .build();
        }
    }
}
