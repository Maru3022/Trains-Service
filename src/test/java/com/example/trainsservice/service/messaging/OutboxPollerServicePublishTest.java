package com.example.trainsservice.service.messaging;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxPollerServicePublishTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OutboxPollerService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OutboxPollerService(repository, kafkaTemplate);
    }

    @Test
    void publishToKafka_success_marksSent() {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setTopic("topic");
        event.setKey("key");
        event.setPayload("payload");
        event.setMaxRetries(3);

        when(repository.findStuckProcessingEvents(any())).thenReturn(Collections.emptyList());
        when(repository.fetchNextPendingEventsForProcessing(50)).thenReturn(List.of(event));
        when(kafkaTemplate.send(eq("topic"), eq("key"), eq("payload")))
                .thenReturn(CompletableFuture.completedFuture(null));

        service.pollOutbox();

        ArgumentCaptor<OutboxEvent> cap = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository, atLeastOnce()).save(cap.capture());
        OutboxEvent saved = cap.getValue();
        assertEquals(OutboxEvent.Status.SENT, saved.getStatus());
        assertNotNull(saved.getProcessedAt());
    }

    @Test
    void publishToKafka_failure_retriesAndDeadLetter() {
        OutboxEvent event1 = new OutboxEvent();
        event1.setId(UUID.randomUUID());
        event1.setTopic("topic");
        event1.setKey("k1");
        event1.setPayload("p1");
        event1.setRetryCount(0);
        event1.setMaxRetries(3);

        OutboxEvent event2 = new OutboxEvent();
        event2.setId(UUID.randomUUID());
        event2.setTopic("topic");
        event2.setKey("k2");
        event2.setPayload("p2");
        event2.setRetryCount(2);
        event2.setMaxRetries(3);

        when(repository.findStuckProcessingEvents(any())).thenReturn(Collections.emptyList());
        when(repository.fetchNextPendingEventsForProcessing(50)).thenReturn(List.of(event1, event2));

        CompletableFuture<Object> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("boom"));

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn((CompletableFuture) failed);

        service.pollOutbox();

        ArgumentCaptor<OutboxEvent> cap = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository, atLeast(2)).save(cap.capture());
        List<OutboxEvent> saved = cap.getAllValues();

        // one should be retried (PENDING) and one moved to DEAD_LETTER
        boolean hasPending = saved.stream().anyMatch(e -> e.getStatus() == OutboxEvent.Status.PENDING && e.getRetryCount() == 1);
        boolean hasDead = saved.stream().anyMatch(e -> e.getStatus() == OutboxEvent.Status.DEAD_LETTER && e.getRetryCount() != null && e.getRetryCount() >= e.getMaxRetries());
        assertTrue(hasPending);
        assertTrue(hasDead);
    }

    @Test
    void resetStuckProcessingEvents_resetsBackToPending() {
        OutboxEvent stuck = new OutboxEvent();
        stuck.setId(UUID.randomUUID());
        stuck.setStatus(OutboxEvent.Status.PROCESSING);
        stuck.setProcessedAt(LocalDateTime.now().minusMinutes(10));
        stuck.setErrorMessage("err");

        when(repository.findStuckProcessingEvents(any())).thenReturn(List.of(stuck));
        when(repository.fetchNextPendingEventsForProcessing(50)).thenReturn(Collections.emptyList());

        service.pollOutbox();

        ArgumentCaptor<List<OutboxEvent>> cap = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(cap.capture());
        List<OutboxEvent> saved = cap.getValue();
        assertEquals(1, saved.size());
        OutboxEvent reset = saved.get(0);
        assertEquals(OutboxEvent.Status.PENDING, reset.getStatus());
        assertNull(reset.getProcessedAt());
        assertNull(reset.getErrorMessage());
    }
}
