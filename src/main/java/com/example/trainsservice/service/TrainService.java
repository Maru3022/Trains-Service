package com.example.trainsservice.service;

import com.example.trainsservice.dto.TrainEventDTO;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.messaging.TrainEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainService {

    private final TrainRepository trainRepository;
    private final TrainEventProducer trainEventProducer;

    public List<Train> getAllTrains(){
        return trainRepository.findAll();
    }

    @Transactional
    public Train saveTrain(Train train){
        Train savedTrain = trainRepository.save(train);
        // Send event
        TrainEventDTO event = new TrainEventDTO(savedTrain.getId().toString(), "CREATED");
        trainEventProducer.sendEvent(event);
        return savedTrain;
    }

    public Optional<Train> getTrainById(Long id){
        return trainRepository.findById(id);
    }

    @Transactional
    public void deleteTrain(Long id){
        trainRepository.deleteById(id);
        // Send event
        TrainEventDTO event = new TrainEventDTO(id.toString(), "DELETED");
        trainEventProducer.sendEvent(event);
    }
}