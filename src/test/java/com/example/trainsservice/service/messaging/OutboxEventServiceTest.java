package com.example.trainsservice.service.messaging;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxEventServiceTest {

    @Mock
    private OutboxEventRepository repository;

    private OutboxEventService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OutboxEventService(repository);
    }

    @Test
    void saveEvent_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.saveEvent(null));
    }

    @Test
    void saveEvent_missingTopicOrPayload_throws() {
        OutboxEvent e = new OutboxEvent();
        e.setTopic(null);
        e.setPayload(null);
        assertThrows(IllegalArgumentException.class, () -> service.saveEvent(e));
    }

    @Test
    void saveEvent_setsDefaults_andSaves() {
        OutboxEvent e = new OutboxEvent();
        e.setTopic("t");
        e.setPayload("p");

        OutboxEvent saved = new OutboxEvent();
        when(repository.save(any())).thenReturn(saved);

        OutboxEvent res = service.saveEvent(e);
        assertNotNull(res);
        verify(repository).save(e);
    }
}
