package com.example.trainsservice;

import com.example.trainsservice.dto.TrainEventDTO;
import com.example.trainsservice.model.OutboxEvent;
import com.example.trainsservice.repository.OutboxEventRepository;
import com.example.trainsservice.service.messaging.TrainEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
        // Given: A train event
        TrainEventDTO event = new TrainEventDTO("train-123", "DEPARTED");
        
        // When: Send event through producer
        producer.sendEvent(event);

        // Then: Event should be saved in outbox with PENDING status
        var events = outboxEventRepository.findByStatusOrderByCreatedAt(OutboxEvent.Status.PENDING);
        assertThat(events).isNotEmpty();
        assertThat(events.get(0).getKey()).isEqualTo("train-123");
        assertThat(events.get(0).getTopic()).isEqualTo("train-events");
        assertThat(events.get(0).getStatus()).isEqualTo(OutboxEvent.Status.PENDING);
    }
}