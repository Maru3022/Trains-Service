package com.example.trainsservice.controller;

import com.example.trainsservice.dto.OneRepMaxResponseDTO;
import com.example.trainsservice.service.CalculatorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/calculator")
@Slf4j
public class CalculatorController {

    private final CalculatorService calculatorService;

    @GetMapping("/1rm")
    public OneRepMaxResponseDTO get1RM(
            @RequestParam double weight,
            @RequestParam int reps
    ){
        log.info("Received 1RM request: weight = {}, reps = {}", weight,reps);
        return calculatorService.calculateOneRepMax(weight,reps);
    }
}