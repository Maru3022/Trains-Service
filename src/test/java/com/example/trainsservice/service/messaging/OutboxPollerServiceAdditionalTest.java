package com.example.trainsservice.service.messaging;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPollerServiceAdditionalTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxPollerService outboxPollerService;

    @Test
    void publishToKafka_success_setsSent() {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setTopic("topic");
        event.setKey("key");
        event.setPayload("payload");
        event.setRetryCount(0);
        event.setMaxRetries(3);

        when(outboxEventRepository.findStuckProcessingEvents(ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        when(outboxEventRepository.fetchNextPendingEventsForProcessing(50)).thenReturn(List.of(event));
        when(outboxEventRepository.markProcessing(ArgumentMatchers.anyList(), ArgumentMatchers.any())).thenReturn(1);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));
        when(outboxEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        outboxPollerService.pollOutbox();

        verify(outboxEventRepository, atLeastOnce()).save(any());
        assertEquals(OutboxEvent.Status.SENT, event.getStatus());
    }

    @Test
    void publishToKafka_failure_retriesAndSetsPending() {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setTopic("topic");
        event.setKey("key");
        event.setPayload("payload");
        event.setRetryCount(0);
        event.setMaxRetries(3);

        when(outboxEventRepository.findStuckProcessingEvents(ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        when(outboxEventRepository.fetchNextPendingEventsForProcessing(50)).thenReturn(List.of(event));
        when(outboxEventRepository.markProcessing(ArgumentMatchers.anyList(), ArgumentMatchers.any())).thenReturn(1);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));
        when(outboxEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        outboxPollerService.pollOutbox();

        verify(outboxEventRepository, atLeastOnce()).save(any());
        assertEquals(OutboxEvent.Status.PENDING, event.getStatus());
        assertNotNull(event.getErrorMessage());
        assertNotNull(event.getProcessedAt());
    }

    @Test
    void publishToKafka_failure_reachesDeadLetter() {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setTopic("topic");
        event.setKey("key");
        event.setPayload("payload");
        event.setRetryCount(2);
        event.setMaxRetries(3);

        when(outboxEventRepository.findStuckProcessingEvents(ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        when(outboxEventRepository.fetchNextPendingEventsForProcessing(50)).thenReturn(List.of(event));
        when(outboxEventRepository.markProcessing(ArgumentMatchers.anyList(), ArgumentMatchers.any())).thenReturn(1);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));
        when(outboxEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        outboxPollerService.pollOutbox();

        verify(outboxEventRepository, atLeastOnce()).save(any());
        assertEquals(OutboxEvent.Status.DEAD_LETTER, event.getStatus());
    }

    @Test
    void resetStuckProcessingEvents_resetsFieldsAndSaves() {
        OutboxEvent stuck = new OutboxEvent();
        stuck.setId(UUID.randomUUID());
        stuck.setStatus(OutboxEvent.Status.PROCESSING);
        stuck.setProcessedAt(LocalDateTime.now().minusHours(1));
        stuck.setErrorMessage("err");

        when(outboxEventRepository.findStuckProcessingEvents(ArgumentMatchers.any())).thenReturn(List.of(stuck));
        when(outboxEventRepository.fetchNextPendingEventsForProcessing(50)).thenReturn(Collections.emptyList());
        when(outboxEventRepository.saveAll(any())).thenReturn(List.of(stuck));

        outboxPollerService.pollOutbox();

        assertEquals(OutboxEvent.Status.PENDING, stuck.getStatus());
        assertNull(stuck.getProcessedAt());
        assertNull(stuck.getErrorMessage());
        verify(outboxEventRepository).saveAll(any());
    }
}
