package com.example.trainsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressUpdateDTO {
    private Long exerciseId;
    private Integer reps;
    private Double weight;
}
