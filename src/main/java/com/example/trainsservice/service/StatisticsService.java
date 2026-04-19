package com.example.trainsservice.service;

import com.example.trainsservice.dto.StatsSummaryResponse;
import com.example.trainsservice.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ProgressRepository progressRepository;

    public StatsSummaryResponse getTrainingSummary() {
        long sets = progressRepository.count();
        double volume = progressRepository.sumVolume();
        return new StatsSummaryResponse(sets, volume);
    }

    public int getRecommendedRest(String category) {
        return switch (category.toLowerCase()) {
            case "strength" -> 180;
            case "hypertrophy" -> 90;
            default -> 60;
        };
    }
}
