package com.example.trainsservice.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class NewTopic {

    @Bean
    public org.apache.kafka.clients.admin.NewTopic trainEventsTopic(){
        System.out.println("LOG: Creating Kafka topic 'trains-evets' with default settings.");
        return TopicBuilder.name("trains-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
