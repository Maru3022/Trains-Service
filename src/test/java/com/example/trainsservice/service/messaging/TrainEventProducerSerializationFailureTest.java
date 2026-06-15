package com.example.trainsservice.service.messaging;

import com.example.trainsservice.dto.TrainEventDTO;
import com.example.trainsservice.service.messaging.TrainEventProducer;
import com.example.trainsservice.service.messaging.OutboxEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TrainEventProducerSerializationFailureTest {

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private ObjectMapper objectMapper;

    private TrainEventProducer producer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        producer = new TrainEventProducer(outboxEventService, objectMapper);
    }

    @Test
    void sendEvent_whenSerializationFails_throwsRuntimeExceptionAndDoesNotSave() throws JsonProcessingException {
        TrainEventDTO dto = new TrainEventDTO("train-1", "started");
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("bad"){});

        assertThrows(RuntimeException.class, () -> producer.sendEvent(dto));
        verify(outboxEventService, never()).saveEvent(any());
    }
}
