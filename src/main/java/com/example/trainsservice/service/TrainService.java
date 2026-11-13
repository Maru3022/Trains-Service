package com.example.trainsservice.service;

import com.example.trainsservice.dto.TrainEventDTO;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.TrainRepository;
import com.example.trainsservice.service.messaging.TrainEventProducer;
import com.example.trainsservice.service.messaging.TrainEventType;
import jakarta.persistence.EntityNotFoundException;
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

    public List<Train> getAllTrains(String userId){
        return trainRepository.findAllByUserId(userId);
    }

    @Transactional
    public Train saveTrain(Train train){
        Train savedTrain = trainRepository.save(train);
        // Send event
        TrainEventDTO event = new TrainEventDTO(savedTrain.getId().toString(), TrainEventType.CREATED.name());
        trainEventProducer.sendEvent(event);
        return savedTrain;
    }

    public Optional<Train> getTrainById(Long id, String userId){
        return trainRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public void deleteTrain(Long id, String userId){
        trainRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new EntityNotFoundException("Train with id " + id + " not found"));
        trainRepository.deleteById(id);
        // Send event
        TrainEventDTO event = new TrainEventDTO(id.toString(), TrainEventType.DELETED.name());
        trainEventProducer.sendEvent(event);
    }
}
