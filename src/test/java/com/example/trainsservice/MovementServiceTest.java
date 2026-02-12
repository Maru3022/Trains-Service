package com.example.trainsservice;

import com.example.trainsservice.dto.ProgressUpdateDTO;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.ProgressRepository;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.MovementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovementServiceTest {

    @Mock private ProgressRepository progressRepository;
    @Mock private TrainRepository trainRepository;
    @InjectMocks private MovementService movementService;

    @Test
    void shouldRegisterSetSuccessfully(){
        ProgressUpdateDTO dto = new ProgressUpdateDTO(1L, 10,100.0);
        when(trainRepository.findById(1L)).thenReturn(Optional.of(new Train()));

        movementService.registerSet(dto);

        verify(progressRepository, times(1)).save(any());
    }
}
