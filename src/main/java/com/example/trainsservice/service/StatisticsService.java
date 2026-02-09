package com.example.trainsservice.service;

import com.example.trainsservice.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatsRepository statsRepository;

    public void updateGlobalStats(
            Double addedWeight
    ){

    }

    private double calculateOneRepMax(
            double weight,
            int reps
    ){
        if(reps <= 1) return weight;
        return weight * (1 + (double) reps / 30);
    }

    public int getRecommendedRest(
            String category
    ){
        return switch (category.toLowerCase()){
            case "strength" -> 180;
            case "hypertrophy" -> 90;
            default -> 60;
        };
    }

}
