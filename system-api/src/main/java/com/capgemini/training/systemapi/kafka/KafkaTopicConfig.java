package com.capgemini.training.systemapi.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Day 23 — Kafka topic definitions.
 * Topics are auto-created on startup (kafka.auto.create.topics.enable=true by default).
 * For production, disable auto-create and use this config exclusively.
 */
@Configuration
public class KafkaTopicConfig {

    public static final String PRODUCT_EVENTS_TOPIC = "product-events";
    public static final String SAGA_EVENTS_TOPIC     = "saga-events";
    public static final String ERROR_EVENTS_TOPIC    = "error-events";

    @Value("${kafka.topics.product-events.partitions:3}")
    private int productPartitions;

    @Value("${kafka.topics.product-events.replicas:1}")
    private short productReplicas;

    @Bean
    public NewTopic productEventsTopic() {
        return TopicBuilder.name(PRODUCT_EVENTS_TOPIC)
            .partitions(productPartitions)
            .replicas(productReplicas)
            .build();
    }

    @Bean
    public NewTopic sagaEventsTopic() {
        return TopicBuilder.name(SAGA_EVENTS_TOPIC)
            .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic errorEventsTopic() {
        return TopicBuilder.name(ERROR_EVENTS_TOPIC)
            .partitions(1).replicas(1).build();
    }
}
