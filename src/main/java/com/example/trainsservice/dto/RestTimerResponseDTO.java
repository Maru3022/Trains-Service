package com.example.trainsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestTimerResponseDTO {

    private int seconds;
    private String message;
}
