package com.example.trainsservice.config;

import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestKafkaConfig {

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> mock = mock(KafkaTemplate.class);
        return mock;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        @SuppressWarnings("unchecked")
        ProducerFactory<String, String> mock = mock(ProducerFactory.class);
        return mock;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        @SuppressWarnings("unchecked")
        ConsumerFactory<String, String> mock = mock(ConsumerFactory.class);
        return mock;
    }
}
