package com.example.trainsservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!bench")
public class KafkaConfig {

    @Bean
    public NewTopic trainsTopic() {
        return TopicBuilder.name("train-events").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic sagaTrainsCommandTopic() {
        return TopicBuilder.name("saga-trains-command").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic sagaTrainsResponseTopic() {
        return TopicBuilder.name("saga-trains-response").partitions(3).replicas(1).build();
    }
}