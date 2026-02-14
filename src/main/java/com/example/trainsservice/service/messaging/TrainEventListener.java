package com.example.trainsservice.service.messaging;

import com.example.trainsservice.dto.TrainEventDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

//@Service
public class TrainEventListener {
    @KafkaListener(topics = "trains-events", groupId = "trains-group")
    public void consume(TrainEventDTO event){
        System.out.println("Received event for train:" + event.getTrainId());
    }
}
