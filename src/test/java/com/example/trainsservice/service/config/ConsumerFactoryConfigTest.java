package com.example.trainsservice.service.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerFactoryConfigTest {

    @Test
    void consumerFactory_withBootstrapServers_returnsFactory() throws Exception {
        ConsumerFactoryConfig cfg = new ConsumerFactoryConfig();
        // set private field bootstrapServers via reflection to avoid @Value injection
        Field f = ConsumerFactoryConfig.class.getDeclaredField("bootstrapServers");
        f.setAccessible(true);
        f.set(cfg, "localhost:9092");

        var factory = cfg.consumerFactory();
        assertNotNull(factory);

        var containerFactory = cfg.kafkaListenerContainerFactory();
        assertNotNull(containerFactory);
    }
}
