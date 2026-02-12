package com.example.trainsservice.service.config;

import com.example.trainsservice.dto.TrainEventDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.jdk.StringSerializer;

import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProducerFactoryConfig {

    @Bean
    public ProducerFactory<String, TrainEventDTO> producerFactory(){
        Map<String,Object> configProps = new HashMap<>();

        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9095");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerialize.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, TrainEventDTO> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }
}
