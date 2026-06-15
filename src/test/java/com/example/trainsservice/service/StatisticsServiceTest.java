package com.example.trainsservice.service;

import com.example.trainsservice.dto.StatsSummaryResponse;
import com.example.trainsservice.repository.ProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsServiceTest {

    @Mock
    private ProgressRepository progressRepository;

    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        statisticsService = new StatisticsService(progressRepository);
    }

    @Test
    void getTrainingSummary_returnsCountsAndVolume() {
        when(progressRepository.count()).thenReturn(10L);
        when(progressRepository.sumVolume()).thenReturn(1234.5);

        StatsSummaryResponse res = statisticsService.getTrainingSummary();
        assertEquals(10L, res.loggedSetsCount());
        assertEquals(1234.5, res.totalVolume());
    }

    @Test
    void getRecommendedRest_variousCategories() {
        assertEquals(180, statisticsService.getRecommendedRest("strength"));
        assertEquals(90, statisticsService.getRecommendedRest("hypertrophy"));
        assertEquals(60, statisticsService.getRecommendedRest("other"));
    }
}
