package com.example.trainsservice.saga;

import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.messaging.OutboxEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaTrainsCommandListener {

    private static final String STEP = "TRAINS";
    private static final String PERSONAL_CABINET = "PERSONAL_CABINET";

    private final TrainRepository trainRepository;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "saga-trains-command", containerFactory = "sagaKafkaListenerContainerFactory",
            groupId = "trains-service-saga")
    @Transactional
    public void onCommand(SagaCommandEvent event, Acknowledgment acknowledgment) {
        log.info("Received saga-trains-command: sagaId={}, status={}", event.getSagaId(), event.getStatus());

        if ("ROLLBACK".equals(event.getStatus())) {
            handleRollback(event);
            return;
        }

        if (!"EXECUTE".equals(event.getStatus())) {
            return;
        }

        Map<String, Object> data = event.getData();
        String userId = data != null ? String.valueOf(data.get("userId")) : null;
        if (userId == null || "null".equals(userId)) {
            publishResponse(event, "FAILED", Map.of("reason", "userId missing in payload"));
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            return;
        }

        Optional<Train> existing = trainRepository.findByUserIdAndCategory(userId, PERSONAL_CABINET);
        Train cabinet = existing.orElseGet(() -> {
            Train t = new Train();
            t.setUserId(userId);
            t.setName("Personal cabinet");
            t.setCategory(PERSONAL_CABINET);
            t.setDurationMinutes(0);
            return trainRepository.save(t);
        });

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("trainId", cabinet.getId());
        publishResponse(event, "SUCCESS", responseData);
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }

    private void handleRollback(SagaCommandEvent event) {
        Map<String, Object> data = event.getData();
        Object trainIdRaw = data != null ? data.get("trainId") : null;
        if (trainIdRaw != null) {
            try {
                Long trainId = Long.valueOf(trainIdRaw.toString());
                trainRepository.deleteById(trainId);
                log.info("Personal cabinet {} deleted as compensation for saga {}", trainId, event.getSagaId());
            } catch (Exception e) {
                log.warn("Could not delete cabinet for rollback of saga {}: {}", event.getSagaId(), e.getMessage());
            }
        }
        publishResponse(event, "ROLLBACK_DONE", null);
    }

    private void publishResponse(SagaCommandEvent command, String status, Map<String, Object> data) {
        try {
            SagaResponseEvent response = new SagaResponseEvent();
            response.setEventId(UUID.randomUUID().toString());
            response.setSagaId(command.getSagaId());
            response.setStep(STEP);
            response.setStatus(status);
            response.setData(data);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType("saga-response");
            outboxEvent.setAggregateId(command.getSagaId());
            outboxEvent.setEventType("SAGA_RESPONSE");
            outboxEvent.setTopic("saga-trains-response");
            outboxEvent.setKey(command.getSagaId());
            outboxEvent.setPayload(objectMapper.writeValueAsString(response));
            outboxEventService.saveEvent(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to publish saga-trains-response: {}", e.getMessage(), e);
        }
    }
}