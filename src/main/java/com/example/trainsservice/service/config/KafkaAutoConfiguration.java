package com.example.trainsservice.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to disable Kafka-related beans during tests.
 * Kafka configurations require active bootstrap servers.
 */
@Configuration
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaAutoConfiguration {
    // This is a marker configuration that controls the enabling of Kafka beans
}
