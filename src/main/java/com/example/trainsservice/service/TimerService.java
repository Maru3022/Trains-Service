package com.example.trainsservice.service;

import com.example.trainsservice.dto.RestTimerResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TimerService {

    public RestTimerResponseDTO getRestTime(
            String intensity
    ){
        log.info("Fetching rest time for intensity level: {} ", intensity);
        int seconds = intensity.equalsIgnoreCase("heavy") ? 100 : 60;

        System.out.println("DEBUG: Timer set to " + seconds + " seconds.");

        return new RestTimerResponseDTO(seconds,"Rest for " + (seconds / 60) + " minutes.");
    }
}
