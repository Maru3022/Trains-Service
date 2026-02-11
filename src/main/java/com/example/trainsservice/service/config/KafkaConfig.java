package com.example.trainsservice.service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic trainsTopic(){
        System.out.println("INFO: Initializing kafka topic 'trains-events' with 3 partitions.");
        return TopicBuilder.name("trains-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
