package com.example.trainsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OneRepMaxResponseDTO {

    private double oneRepMax;
    private String formulaUsed;
}
