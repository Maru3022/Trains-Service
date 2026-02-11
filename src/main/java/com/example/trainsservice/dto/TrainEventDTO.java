package com.example.trainsservice.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TrainEventDTO {

    private String trainId;
    private String status;

    public TrainEventDTO() {}

    public String getTrainId(){
        return getTrainId();
    }

    public void setTrainId(
            String trainId
    ){
        this.trainId = trainId;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(
            String status
    ){
        this.status = status;
    }
}
