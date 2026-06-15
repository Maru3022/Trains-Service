package com.example.trainsservice.service;

import com.example.trainsservice.dto.RestTimerResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerServiceTest {

    private TimerService timerService;

    @BeforeEach
    void setUp() {
        timerService = new TimerService();
    }

    @Test
    void getRestTime_heavy_returns100() {
        RestTimerResponseDTO dto = timerService.getRestTime("heavy");
        assertEquals(100, dto.getSeconds());
        assertTrue(dto.getMessage().contains("Rest"));
    }

    @Test
    void getRestTime_light_returns60() {
        RestTimerResponseDTO dto = timerService.getRestTime("light");
        assertEquals(60, dto.getSeconds());
    }
}
