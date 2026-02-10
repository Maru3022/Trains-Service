package com.example.trainsservice.service;

import com.example.trainsservice.dto.OneRepMaxResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CalculatorService {

    public OneRepMaxResponseDTO calculateOneRepMax(
            double weight,
            int reps
    ){
        log.info("Calculating 1RM for weight: {} and reps: {}", weight,reps);

        double result = weight * (1 + (double) reps / 30);
        System.out.println("DEBUG: Calculation finished. Result: " + result);
        return new OneRepMaxResponseDTO(result,"Epley");
    }
}
