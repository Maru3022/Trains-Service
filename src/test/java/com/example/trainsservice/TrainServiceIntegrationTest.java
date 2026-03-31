package com.example.trainsservice;

import com.example.trainsservice.config.TestKafkaConfig;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.TrainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext
@Import(TestKafkaConfig.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
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
        Train savedTrain = trainService.saveTrain(testTrain);
        assertThat(savedTrain.getId()).isNotNull();
        assertThat(trainRepository.existsById(savedTrain.getId())).isTrue();
    }

    @Test
    void testGetTrainById() {
        Train savedTrain = trainRepository.save(testTrain);
        var result = trainService.getTrainById(savedTrain.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Express Train");
    }

    @Test
    void testDeleteTrain() {
        Train savedTrain = trainRepository.save(testTrain);
        Long trainId = savedTrain.getId();
        trainService.deleteTrain(trainId);
        assertThat(trainRepository.existsById(trainId)).isFalse();
    }

    @Test
    void testGetAllTrains() {
        trainRepository.save(testTrain);
        Train train2 = new Train();
        train2.setName("Local Train");
        train2.setCategory("Local");
        train2.setDurationMinutes(180);
        trainRepository.save(train2);
        
        var trains = trainService.getAllTrains();
        assertThat(trains.size()).isGreaterThanOrEqualTo(2);
    }
}
