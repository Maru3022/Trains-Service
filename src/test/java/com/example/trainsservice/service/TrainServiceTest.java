package com.example.trainsservice.service;

import com.example.trainsservice.dto.TrainEventDTO;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.messaging.TrainEventProducer;
import com.example.trainsservice.service.messaging.TrainEventType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainServiceTest {

    @Mock
    private TrainRepository trainRepository;

    @Mock
    private TrainEventProducer trainEventProducer;

    private TrainService trainService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainService = new TrainService(trainRepository, trainEventProducer);
    }

    @Test
    void getAllTrains_returnsList() {
        when(trainRepository.findAll()).thenReturn(List.of(new Train()));
        List<Train> result = trainService.getAllTrains();
        assertEquals(1, result.size());
        verify(trainRepository).findAll();
    }

    @Test
    void saveTrain_savesAndSendsEvent() {
        Train t = new Train();
        t.setId(5L);
        when(trainRepository.save(t)).thenReturn(t);

        Train saved = trainService.saveTrain(t);
        assertEquals(5L, saved.getId());

        ArgumentCaptor<TrainEventDTO> captor = ArgumentCaptor.forClass(TrainEventDTO.class);
        verify(trainEventProducer).sendEvent(captor.capture());
        TrainEventDTO dto = captor.getValue();
        assertEquals("5", dto.getTrainId());
        assertEquals(TrainEventType.CREATED.name(), dto.getStatus());
    }

    @Test
    void deleteTrain_notFound_throws() {
        when(trainRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> trainService.deleteTrain(10L));
    }

    @Test
    void deleteTrain_success_callsDeleteAndEvent() {
        Train t = new Train();
        t.setId(3L);
        when(trainRepository.findById(3L)).thenReturn(Optional.of(t));

        trainService.deleteTrain(3L);

        verify(trainRepository).deleteById(3L);
        ArgumentCaptor<TrainEventDTO> captor = ArgumentCaptor.forClass(TrainEventDTO.class);
        verify(trainEventProducer).sendEvent(captor.capture());
        assertEquals("3", captor.getValue().getTrainId());
        assertEquals(TrainEventType.DELETED.name(), captor.getValue().getStatus());
    }
}
