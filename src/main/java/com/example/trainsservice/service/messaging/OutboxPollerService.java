package com.example.trainsservice.service.messaging;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.CompletableFuture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!bench")
@ConditionalOnProperty(name = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPollerService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.poller.interval-ms:1000}")
    @Transactional
    public void pollOutbox() {
        try {
            resetStuckProcessingEvents();
            List<OutboxEvent> events = outboxEventRepository.fetchNextPendingEventsForProcessing(50);
            if (events.isEmpty()) {
                return;
            }

            List<UUID> eventIds = events.stream()
                    .map(OutboxEvent::getId)
                    .collect(Collectors.toList());
            outboxEventRepository.markProcessing(eventIds, LocalDateTime.now());

            for (OutboxEvent event : events) {
                publishToKafka(event);
            }
        } catch (Exception e) {
            log.error("Failed to poll outbox events", e);
        }
    }

    private void resetStuckProcessingEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<OutboxEvent> stuckEvents = outboxEventRepository.findStuckProcessingEvents(cutoff);
        if (stuckEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : stuckEvents) {
            event.setStatus(OutboxEvent.Status.PENDING);
            event.setProcessedAt(null);
            event.setErrorMessage(null);
        }
        outboxEventRepository.saveAll(stuckEvents);
        log.info("Reset {} stuck outbox events back to PENDING", stuckEvents.size());
    }

    private void publishToKafka(OutboxEvent event) {
        kafkaTemplate.send(event.getTopic(), event.getKey(), event.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        event.setStatus(OutboxEvent.Status.SENT);
                        event.setProcessedAt(LocalDateTime.now());
                        event.setErrorMessage(null);
                        outboxEventRepository.save(event);
                        log.info("Outbox event {} sent to topic {}", event.getId(), event.getTopic());
                        return;
                    }

                    int retryCount = event.getRetryCount() == null ? 0 : event.getRetryCount();
                    retryCount++;
                    event.setRetryCount(retryCount);
                    event.setErrorMessage(ex.getMessage());
                    if (retryCount >= event.getMaxRetries()) {
                        event.setStatus(OutboxEvent.Status.DEAD_LETTER);
                        log.error("Outbox event {} reached max retries and moved to DEAD_LETTER", event.getId(), ex);
                    } else {
                        event.setStatus(OutboxEvent.Status.PENDING);
                        log.warn("Outbox event {} failed send and will be retried (retryCount={})", event.getId(), retryCount);
                    }
                    event.setProcessedAt(LocalDateTime.now());
                    outboxEventRepository.save(event);
                });
    }
}
