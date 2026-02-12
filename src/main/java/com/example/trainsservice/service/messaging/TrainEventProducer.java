package com.example.trainsservice.service.messaging;

import com.example.trainsservice.dto.TrainEventDTO;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TrainEventProducer {
    private final KafkaTemplate<String, TrainEventDTO> kafkaTemplate;

    public void sendEvent(TrainEventDTO event){
        kafkaTemplate.send("trains-events",event.getTrainId(),event);
    }
}
