package com.example.trainsservice;

import com.example.trainsservice.config.TestKafkaConfig;
import com.example.trainsservice.dto.ProgressUpdateDTO;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.ProgressRepository;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.MovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, excludeAutoConfiguration = KafkaAutoConfiguration.class)
@ActiveProfiles("test")
@DirtiesContext
@Import(TestKafkaConfig.class)
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
        ProgressUpdateDTO dto = new ProgressUpdateDTO(testTrain.getId(), 10, 100.0);
        movementService.registerSet(dto);
        var allProgress = progressRepository.findAll();
        assertThat(allProgress).isNotEmpty();
    }

    @Test
    void testRegisterSetWithInvalidTrainId() {
        ProgressUpdateDTO dto = new ProgressUpdateDTO(999L, 10, 100.0);
        assertThatThrownBy(() -> movementService.registerSet(dto))
                .isInstanceOf(RuntimeException.class);
    }
}
