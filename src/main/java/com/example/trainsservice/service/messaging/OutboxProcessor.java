package com.example.trainsservice.service.messaging;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        try {
            List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAt(OutboxEvent.Status.PENDING);

            if (pendingEvents.isEmpty()) {
                log.debug("No pending events to process");
                return;
            }

            for (OutboxEvent event : pendingEvents) {
                try {
                    stringKafkaTemplate.send(event.getTopic(), event.getKey(), event.getPayload());
                    event.setStatus(OutboxEvent.Status.SENT);
                    event.setProcessedAt(LocalDateTime.now());
                    outboxEventRepository.save(event);
                    log.info("Sent event to Kafka: {}", event.getId());
                } catch (Exception e) {
                    log.error("Failed to send event {}: {}", event.getId(), e.getMessage());
                    event.setStatus(OutboxEvent.Status.FAILED);
                    outboxEventRepository.save(event);
                }
            }
        } catch (Exception e) {
            log.error("Error in outbox processor: {}", e.getMessage(), e);
        }
    }
}