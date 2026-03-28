package com.example.trainsservice;

import com.example.trainsservice.dto.TrainEventDTO;
import com.example.trainsservice.service.messaging.TrainEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EmbeddedKafka(partitions = 1,
        brokerProperties = "listeners=PLAINTEXT://localhost:9092")
@DirtiesContext
@ActiveProfiles("test")
public class TrainMessagingTest {

    @Autowired
    private TrainEventProducer producer;

    @Test
    void testSendEvent() {
        TrainEventDTO event = new TrainEventDTO("train-123", "DEPARTED");
        producer.sendEvent(event);
    }
}