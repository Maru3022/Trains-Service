package com.example.trainsservice.service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.context.annotation.Bean;

@Configuration("serviceKafkaConfig")
public class KafkaConfig {

    @Bean("trainEventsTopic")
    public NewTopic trainEventsTopic() {
        return TopicBuilder.name("train-events").partitions(3).replicas(1).build();
    }
}