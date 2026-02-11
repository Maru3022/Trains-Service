package com.example.trainsservice.service.messaging;

import com.example.trainsservice.dto.TrainEventDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TrainEventProducer {

    private final KafkaTemplate<String, TrainEventDTO> kafkaTemplate;

    public void sendEvent(
            TrainEventDTO event
    ){
        kafkaTemplate.send("trains-events", event.getTrainId(), event)
                .whenComplete((result, ex) -> {
                    if(ex == null){
                        System.out.println("SUCCESS: Message sent to topic " + result.getRecordMetadata().topic());
                    }else{
                        System.out.println("ERROR: Failed to send message due to: " + ex.getMessage());
                    }
                });
    }
}
