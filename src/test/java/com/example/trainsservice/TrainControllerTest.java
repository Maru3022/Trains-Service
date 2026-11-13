package com.example.trainsservice;

import com.example.trainsservice.config.TestKafkaConfig;
import com.example.trainsservice.controller.TrainController;
import com.example.trainsservice.model.Train;
import com.example.trainsservice.service.TrainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(TestKafkaConfig.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
public class TrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainService trainService;

    @Test
    void getTrainById_WhenFound_ShouldReturnTrain() throws Exception {
        Train train = new Train();
        when(trainService.getTrainById(1L, "user-1"))
                .thenReturn(Optional.of(train));

        mockMvc.perform(get("/api/v1/plans/1").header("X-User-Id", "user-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainById_WhenNotFound_ShouldReturnTrain() throws Exception {
        when(trainService.getTrainById(1L, "user-1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/plans/1").header("X-User-Id", "user-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTrains_WhenCalled_ShouldReturnList() throws Exception {
        Train train = new Train();
        when(trainService.getAllTrains("user-1")).thenReturn(List.of(train));

        mockMvc.perform(get("/api/v1/plans").header("X-User-Id", "user-1"))
                .andExpect(status().isOk());
    }

    @Test
    void createTrain_ShouldReturnCreatedAndLocation() throws Exception {
        String json = "{\"name\":\"Express\",\"category\":\"fast\",\"durationMinutes\":120,\"userId\":\"user-1\"}";
        Train saved = new Train();
        saved.setId(10L);
        saved.setName("Express");

        when(trainService.saveTrain(org.mockito.ArgumentMatchers.any(Train.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/plans")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    String location = result.getResponse().getHeader("Location");
                    if (location == null) throw new AssertionError("Location header missing");
                });
    }

    @Test
    void deleteTrain_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/plans/1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/v1/plans/1").header("X-User-Id", "user-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getTrainById_DifferentUser_ShouldNotExposePlan() throws Exception {
        Train victimPlan = new Train();
        victimPlan.setId(1L);
        victimPlan.setUserId("victim");
        when(trainService.getTrainById(1L, "attacker")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/plans/1")
                        .header("X-User-Id", "attacker"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTrain_DifferentUser_ShouldNotDeletePlan() throws Exception {
        Train victimPlan = new Train();
        victimPlan.setId(1L);
        victimPlan.setUserId("victim");
        doThrow(new jakarta.persistence.EntityNotFoundException()).when(trainService).deleteTrain(1L, "attacker");

        mockMvc.perform(delete("/api/v1/plans/1")
                        .header("X-User-Id", "attacker"))
                .andExpect(status().isNotFound());

        verify(trainService).deleteTrain(1L, "attacker");
    }
}
