package com.example.trainsservice.controller;

import com.example.trainsservice.model.Train;
import com.example.trainsservice.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trains")
public class TrainController {
    private final TrainService trainService;

    @GetMapping
    public List<Train> getTrains(){
        return trainService.getAllTrains();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Train> getTrainById(
            @PathVariable Long id
    ){
        return trainService.getTrainById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Train createTrain(
            @RequestBody Train train
    ){
        return trainService.saveTrain(train);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrain(
            @PathVariable Long id
    ){
        trainService.deleteTrain(id);
        return ResponseEntity.noContent().build();
    }
}
