package com.example.trainsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class TrainEventDTO {
    private String trainId;
    private String status;

    public TrainEventDTO(){}
}
