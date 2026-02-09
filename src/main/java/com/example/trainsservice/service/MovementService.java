package com.example.trainsservice.service;

import com.example.trainsservice.dto.ProgressUpdateDTO;
import com.example.trainsservice.model.Progress;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.repository.ProgressRepository;
import com.example.trainsservice.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MovementService {

    private final ProgressRepository progressRepository;
    private final TrainRepository trainRepository;

    public Progress logWorkout(
            Long trainId,
            Integer reps,
            Double weight
    ){
        Progress progress = new Progress();

        return progressRepository.save(progress);
    }

    public void registerSet(
            ProgressUpdateDTO dto
    ){
        Train train = trainRepository.findById(dto.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + dto.getExerciseId()));

        Progress progress = new Progress();
        progress.setTrain(train);
        progress.setReps(dto.getReps());
        progress.setWeight(dto.getWeight());
        progress.setDate(LocalDate.from(LocalDateTime.now()));

        progressRepository.save(progress);

    }

}
