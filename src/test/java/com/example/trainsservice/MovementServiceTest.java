package com.example.trainsservice;

import com.example.trainsservice.dto.ProgressUpdateDTO;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.ProgressRepository;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.MovementService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovementServiceTest {

    @Mock private ProgressRepository progressRepository;
    @Mock private TrainRepository trainRepository;
    @InjectMocks private MovementService movementService;

    @Test
    void shouldRegisterSetSuccessfully(){
        ProgressUpdateDTO dto = new ProgressUpdateDTO(1L, 10,100.0);
        when(trainRepository.findByIdAndUserId(1L, "user-1")).thenReturn(Optional.of(new Train()));

        movementService.registerSet(dto, "user-1");

        verify(progressRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowWhenTrainMissing() {
        ProgressUpdateDTO dto = new ProgressUpdateDTO(99L, 5, 50.0);
        when(trainRepository.findByIdAndUserId(99L, "user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movementService.registerSet(dto, "user-1"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}
