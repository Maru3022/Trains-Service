package com.example.trainsservice;

import com.example.trainsservice.dto.ProgressUpdateDTO;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.ProgressRepository;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.MovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class MovementServiceIntegrationTest {

    @Autowired
    private MovementService movementService;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private TrainRepository trainRepository;

    private Train testTrain;

    @BeforeEach
    void setUp() {
        testTrain = new Train();
        testTrain.setName("Test Train");
        testTrain.setCategory("Test");
        testTrain.setDurationMinutes(60);
        testTrain = trainRepository.save(testTrain);
    }

    @Test
    void testRegisterSetSuccessfully() {
        // Given: A valid progress update
        ProgressUpdateDTO dto = new ProgressUpdateDTO(testTrain.getId(), 10, 100.0);

        // When: Register the set
        movementService.registerSet(dto);

        // Then: Progress should be saved
        var allProgress = progressRepository.findAll();
        assertThat(allProgress).isNotEmpty();
        assertThat(allProgress.get(allProgress.size() - 1).getReps()).isEqualTo(10);
        assertThat(allProgress.get(allProgress.size() - 1).getWeight()).isEqualTo(100.0);
    }

    @Test
    void testRegisterSetWithInvalidTrainId() {
        // Given: An invalid train id
        ProgressUpdateDTO dto = new ProgressUpdateDTO(999L, 10, 100.0);

        // When & Then: Should throw an exception
        assertThatThrownBy(() -> movementService.registerSet(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }
}
