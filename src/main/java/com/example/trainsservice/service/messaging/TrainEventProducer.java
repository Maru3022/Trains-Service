package com.example.trainsservice.service.messaging;

import com.example.trainsservice.dto.TrainEventDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TrainEventProducer {

    private final KafkaTemplate<?, ?> kafkaTemplate;

    public TrainEventProducer(KafkaTemplate<?, ?> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(TrainEventDTO event) {
        ((KafkaTemplate<String, Object>) kafkaTemplate).send("train-events", event.getTrainId(), event);
    }
}