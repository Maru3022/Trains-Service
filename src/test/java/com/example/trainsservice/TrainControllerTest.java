package com.example.trainsservice;

import com.example.trainsservice.controller.TrainController;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.service.TrainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainController.class)
@ActiveProfiles("test")
public class TrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainService trainService;

    @Test
    void getTrainById_WhenFound_ShouldReturnTrain() throws Exception {
        Train train = new Train();
        when(trainService.getTrainById(1L))
                .thenReturn(Optional.of(train));

        mockMvc.perform(get("/api/trains/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainById_WhenNotFound_ShouldReturnTrain() throws Exception {
        when(trainService.getTrainById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trains/1"))
                .andExpect(status().isNotFound());
    }
}