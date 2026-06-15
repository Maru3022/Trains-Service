package com.example.trainsservice.service.messaging;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class OutboxPollerServiceTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OutboxPollerService poller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        poller = new OutboxPollerService(repository, kafkaTemplate);
    }

    @Test
    void pollOutbox_noEvents_noPublish() {
        when(repository.fetchNextPendingEventsForProcessing(50)).thenReturn(List.of());
        poller.pollOutbox();
        verify(repository).fetchNextPendingEventsForProcessing(50);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void pollOutbox_withEvent_publishesAndSaves() {
        OutboxEvent e = new OutboxEvent();
        e.setId(UUID.randomUUID());
        e.setTopic("topic");
        e.setKey("k");
        e.setPayload("p");
        e.setMaxRetries(3);

        when(repository.fetchNextPendingEventsForProcessing(50)).thenReturn(List.of(e));
        when(repository.markProcessing(any(), any(LocalDateTime.class))).thenReturn(1);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn((CompletableFuture) CompletableFuture.completedFuture(null));

        poller.pollOutbox();

        // ensure publish attempted and save called (via whenComplete path)
        verify(kafkaTemplate).send("topic", "k", "p");
        verify(repository, atLeastOnce()).save(any(OutboxEvent.class));
    }
}
