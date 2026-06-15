package com.example.trainsservice.service.messaging;

import com.example.trainsservice.dto.TrainEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainEventProducerSuccessTest {

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private ObjectMapper objectMapper;

    private TrainEventProducer producer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        producer = new TrainEventProducer(outboxEventService, objectMapper);
    }

    @Test
    void sendEvent_savesToOutbox() {
        TrainEventDTO dto = new TrainEventDTO("train-1", "started");
        producer.sendEvent(dto);
        verify(outboxEventService).saveEvent(any());
    }
}
