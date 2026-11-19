package com.example.trainsservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainRequestDTO {

    private String name;
    private String category;
    private Integer durationMinutes;
}
