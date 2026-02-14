package com.example.trainsservice;

import com.example.trainsservice.controller.TrainController;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.service.TrainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(TrainController.class)
public class TrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainService trainService;

    @Test
    void getTrainById_WhenFound_ShouldReturnTrain() throws Exception{
        Train train = new Train();
        when(trainService.getTrainById(1L))
                .thenReturn(Optional.of(train));

        mockMvc.perform(get("/api/trains/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainById_WhenNotFound_ShouldReturnTrain() throws Exception{
        when(trainService.getTrainById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trains/1"))
                .andExpect(status().isNotFound());
    }
}
