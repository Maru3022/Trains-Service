package com.example.trainsservice;

import com.example.trainsservice.dto.OneRepMaxResponseDTO;
import com.example.trainsservice.service.CalculatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CalculatorServiceTest {

    private final CalculatorService calculatorService = new CalculatorService();

    @Test
    void shouldCalculateCorrect1RM(){
        OneRepMaxResponseDTO result = calculatorService.calculateOneRepMax(100.0,10);
        assertEquals(133.33, result.getOneRepMax(),0.01);
        assertEquals("Epley",result.getMethod());
    }

}
