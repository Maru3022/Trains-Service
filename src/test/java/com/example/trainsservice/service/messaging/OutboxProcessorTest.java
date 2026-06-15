package com.example.trainsservice.service.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OutboxProcessorTest {

    @Test
    void instantiation_exists() {
        OutboxProcessor p = new OutboxProcessor();
        assertNotNull(p);
    }
}
