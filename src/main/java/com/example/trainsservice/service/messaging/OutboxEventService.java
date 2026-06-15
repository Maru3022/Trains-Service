package com.example.trainsservice.service.messaging;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent saveEvent(OutboxEvent outboxEvent) {
        if (outboxEvent == null) {
            throw new IllegalArgumentException("Outbox event must not be null");
        }
        if (outboxEvent.getTopic() == null || outboxEvent.getPayload() == null) {
            throw new IllegalArgumentException("Outbox event topic and payload must not be null");
        }

        if (outboxEvent.getStatus() == null) {
            outboxEvent.setStatus(OutboxEvent.Status.PENDING);
        }
        if (outboxEvent.getRetryCount() == null) {
            outboxEvent.setRetryCount(0);
        }
        if (outboxEvent.getMaxRetries() == null) {
            outboxEvent.setMaxRetries(3);
        }
        OutboxEvent saved = outboxEventRepository.save(outboxEvent);
        log.info("Saved outbox event {} for topic {}", saved.getId(), saved.getTopic());
        return saved;
    }
}
