package com.example.trainsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OneRepMaxResponseDTO {

    private double oneRepMax;
    private String method;
}
