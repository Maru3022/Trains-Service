package com.example.trainsservice.controller;

import com.example.trainsservice.dto.StatsSummaryResponse;
import com.example.trainsservice.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RequestMapping("/api/v1/stats")
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatisticsService statisticsService;

    @GetMapping("/summary")
    public StatsSummaryResponse getGeneralStats(@RequestHeader("X-User-Id") String userId) {
        return statisticsService.getTrainingSummary(userId);
    }
}
