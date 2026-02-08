package com.example.trainsservice.controller;

import com.example.trainsservice.model.Train;
import com.example.trainsservice.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
