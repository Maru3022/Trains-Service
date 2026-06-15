package com.example.trainsservice.service.config;

import com.example.trainsservice.config.SagaMessagingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import static org.junit.jupiter.api.Assertions.*;

class ConfigBeansTest {

    @Test
    void sagaMessagingConfig_createsBeans() {
        SagaMessagingConfig cfg = new SagaMessagingConfig();
        KafkaProperties props = new KafkaProperties();
        var consumerFactory = cfg.sagaConsumerFactory(props);
        assertNotNull(consumerFactory);

        ConcurrentKafkaListenerContainerFactory<String, String> containerFactory = cfg.sagaKafkaListenerContainerFactory(consumerFactory);
        assertNotNull(containerFactory);
        assertNotNull(containerFactory.getContainerProperties());
    }
}
