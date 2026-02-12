package com.example.trainsservice.service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic trainsTopic(){
        return TopicBuilder.name("trains-evets")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
