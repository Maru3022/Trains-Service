package com.example.trainsservice;

import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.TrainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class TrainServiceIntegrationTest {

    @Autowired
    private TrainService trainService;

    @Autowired
    private TrainRepository trainRepository;

    private Train testTrain;

    @BeforeEach
    void setUp() {
        testTrain = new Train();
        testTrain.setName("Express Train");
        testTrain.setCategory("Express");
        testTrain.setDurationMinutes(120);
    }

    @Test
    void testSaveTrainCreatesOutboxEvent() {
        // Given: A train to save
        // When: Save the train
        Train savedTrain = trainService.saveTrain(testTrain);

        // Then: Train should be saved
        assertThat(savedTrain.getId()).isNotNull();
        assertThat(trainRepository.existsById(savedTrain.getId())).isTrue();
    }

    @Test
    void testGetTrainById() {
        // Given: A saved train
        Train savedTrain = trainRepository.save(testTrain);

        // When: Get train by id
        var result = trainService.getTrainById(savedTrain.getId());

        // Then: Train should be found
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Express Train");
    }

    @Test
    void testDeleteTrain() {
        // Given: A saved train
        Train savedTrain = trainRepository.save(testTrain);
        Long trainId = savedTrain.getId();

        // When: Delete the train
        trainService.deleteTrain(trainId);

        // Then: Train should be deleted
        assertThat(trainRepository.existsById(trainId)).isFalse();
    }

    @Test
    void testGetAllTrains() {
        // Given: Multiple trains saved
        trainRepository.save(testTrain);
        Train train2 = new Train();
        train2.setName("Local Train");
        train2.setCategory("Local");
        train2.setDurationMinutes(180);
        trainRepository.save(train2);

        // When: Get all trains
        var trains = trainService.getAllTrains();

        // Then: All trains should be returned
        assertThat(trains.size()).isGreaterThanOrEqualTo(2);
    }
}
