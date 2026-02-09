package com.example.trainsservice.dto;

import lombok.Data;

@Data
public class ProgressUpdateDTO {
    private Long exerciseId;
    private Integer reps;
    private Double weight;
}
