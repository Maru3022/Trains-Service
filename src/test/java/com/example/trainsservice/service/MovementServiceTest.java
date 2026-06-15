package com.example.trainsservice.service;

import com.example.trainsservice.dto.ProgressUpdateDTO;
import com.example.trainsservice.model.Progress;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.ProgressRepository;
import com.example.trainsservice.repository.TrainRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovementServiceTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private TrainRepository trainRepository;

    private MovementService movementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        movementService = new MovementService(progressRepository, trainRepository);
    }

    @Test
    void registerSet_savesProgress() {
        Train train = new Train();
        train.setId(7L);
        when(trainRepository.findById(7L)).thenReturn(Optional.of(train));

        ProgressUpdateDTO dto = new ProgressUpdateDTO();
        dto.setExerciseId(7L);
        dto.setReps(5);
        dto.setWeight(100.0);

        movementService.registerSet(dto);

        verify(progressRepository).save(any(Progress.class));
    }

    @Test
    void registerSet_missingTrain_throws() {
        when(trainRepository.findById(99L)).thenReturn(Optional.empty());
        ProgressUpdateDTO dto = new ProgressUpdateDTO();
        dto.setExerciseId(99L);
        assertThrows(EntityNotFoundException.class, () -> movementService.registerSet(dto));
    }
}
