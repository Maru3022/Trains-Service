package com.example.trainsservice.service.messaging;

import com.example.trainsservice.dto.TrainEventDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TrainEventListenerTest {

    @Test
    void consume_printsWithoutError() {
        TrainEventListener listener = new TrainEventListener();
        TrainEventDTO dto = new TrainEventDTO("train-1", "STARTED");
        assertDoesNotThrow(() -> listener.consume(dto));
    }
}
