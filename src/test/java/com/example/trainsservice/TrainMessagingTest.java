package com.example.trainsservice;

import com.example.trainsservice.dto.TrainEventDTO;
import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import com.example.trainsservice.service.messaging.TrainEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1,
        brokerProperties = "listeners=PLAINTEXT://localhost:9092")
@DirtiesContext
@ActiveProfiles("test")
public class TrainMessagingTest {

    @Autowired
    private TrainEventProducer producer;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @Transactional
    void testSendEvent() {
        TrainEventDTO event = new TrainEventDTO("train-123", "DEPARTED");
        producer.sendEvent(event);

        // Check that event is saved in outbox
        var events = outboxEventRepository.findByStatusOrderByCreatedAt(OutboxEvent.Status.PENDING);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getKey()).isEqualTo("train-123");
        assertThat(events.get(0).getTopic()).isEqualTo("train-events");
    }
}