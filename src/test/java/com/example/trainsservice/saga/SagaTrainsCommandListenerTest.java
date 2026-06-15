package com.example.trainsservice.saga;

import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.messaging.OutboxEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SagaTrainsCommandListenerTest {

    @Mock
    private TrainRepository trainRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Acknowledgment ack;

    private SagaTrainsCommandListener listener;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        listener = new SagaTrainsCommandListener(trainRepository, outboxEventService, objectMapper);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    }

    @Test
    void onCommand_execute_missingUserId_publishesFailedAndAcks() {
        SagaCommandEvent ev = new SagaCommandEvent();
        ev.setSagaId("saga-1");
        ev.setStatus("EXECUTE");
        ev.setData(null);

        listener.onCommand(ev, ack);

        verify(outboxEventService).saveEvent(any());
        verify(ack).acknowledge();
    }

    @Test
    void onCommand_execute_existingTrain_publishesSuccessAndAcks() {
        SagaCommandEvent ev = new SagaCommandEvent();
        ev.setSagaId("saga-2");
        ev.setStatus("EXECUTE");
        ev.setData(Map.of("userId", "user-1"));

        Train t = new Train();
        t.setId(42L);
        when(trainRepository.findByUserIdAndCategory("user-1", "PERSONAL_CABINET")).thenReturn(Optional.of(t));

        listener.onCommand(ev, ack);

        verify(trainRepository, never()).save(any());
        verify(outboxEventService).saveEvent(any());
        verify(ack).acknowledge();
    }

    @Test
    void onCommand_execute_createsTrain_whenMissing() {
        SagaCommandEvent ev = new SagaCommandEvent();
        ev.setSagaId("saga-3");
        ev.setStatus("EXECUTE");
        ev.setData(Map.of("userId", "user-2"));

        when(trainRepository.findByUserIdAndCategory("user-2", "PERSONAL_CABINET")).thenReturn(Optional.empty());
        when(trainRepository.save(any())).thenAnswer(inv -> {
            Train tt = inv.getArgument(0);
            tt.setId(100L);
            return tt;
        });

        listener.onCommand(ev, ack);

        verify(trainRepository).save(any());
        verify(outboxEventService).saveEvent(any());
        verify(ack).acknowledge();
    }

    @Test
    void onCommand_rollback_deletesTrainAndPublishes() {
        SagaCommandEvent ev = new SagaCommandEvent();
        ev.setSagaId("saga-4");
        ev.setStatus("ROLLBACK");
        ev.setData(Map.of("trainId", "55"));

        listener.onCommand(ev, ack);

        verify(trainRepository).deleteById(55L);
        verify(outboxEventService).saveEvent(any());
    }
}
