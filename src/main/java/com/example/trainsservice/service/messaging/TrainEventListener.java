package com.example.trainsservice.service.messaging;

import com.example.trainsservice.dto.TrainEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrainEventListener {

    @KafkaListener(topics = "trains-events", groupId = "trains-group")
    public void listen(
            TrainEventDTO event
    ){
        log.info("Received Kafka message from 'train-events' topic");

        System.out.println("EVENT CONSUMED: ");
        System.out.println(" - Train ID: " + event.getTrainId());
        System.out.println("- Current Status: " + event.getStatus());
        System.out.println("---------------------------------------------");
    }

}