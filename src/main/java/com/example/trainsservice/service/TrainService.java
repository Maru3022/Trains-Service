package com.example.trainsservice.service;

import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainService {

    private final TrainRepository trainRepository;

    public List<Train> getAllTrains(){
        return trainRepository.findAll();
    }

    public Train saveTrain(
            Train train
    ){
        return trainRepository.save(train);
    }

    public Optional<Train> getTrainById(
            Long id
    ){
        return trainRepository.findById(id);
    }

    public void deleteTrain(
            Long id
    ){
        trainRepository.deleteById(id);
    }
}
