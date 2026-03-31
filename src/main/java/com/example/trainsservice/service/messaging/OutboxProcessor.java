package com.example.trainsservice.service.messaging;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${spring.task.scheduling.enabled:true}")
    private boolean schedulingEnabled;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        if (!schedulingEnabled) {
            return;
        }
        try {
            List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAt(OutboxEvent.Status.PENDING);

            if (pendingEvents.isEmpty()) {
                return;
            }

            for (OutboxEvent event : pendingEvents) {
                try {
                    kafkaTemplate.send(event.getTopic(), event.getKey(), event.getPayload());
                    event.setStatus(OutboxEvent.Status.SENT);
                    event.setProcessedAt(LocalDateTime.now());
                    outboxEventRepository.save(event);
                    log.info("Sent event: {}", event.getId());
                } catch (Exception e) {
                    log.error("Error sending event {}: {}", event.getId(), e.getMessage());
                    event.setStatus(OutboxEvent.Status.FAILED);
                    outboxEventRepository.save(event);
                }
            }
        } catch (Exception e) {
            log.error("Error in outbox processor", e);
        }
    }
}