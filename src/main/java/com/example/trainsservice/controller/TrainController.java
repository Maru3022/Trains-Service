package com.example.trainsservice.controller;

import com.example.trainsservice.model.Train;
import com.example.trainsservice.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plans")
public class TrainController {
    private final TrainService trainService;

    @GetMapping
    public List<Train> getTrains(@RequestHeader("X-User-Id") String userId){
        return trainService.getAllTrains(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Train> getTrainById(
            @PathVariable Long id
            , @RequestHeader("X-User-Id") String userId
    ){
        return trainService.getTrainById(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Train> createTrain(
            @RequestBody Train train
    ){
        Train savedTrain = trainService.saveTrain(train);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedTrain.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedTrain);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrain(
            @PathVariable Long id
            , @RequestHeader("X-User-Id") String userId
    ){
        trainService.deleteTrain(id, userId);
        return ResponseEntity.noContent().build();
    }
}
