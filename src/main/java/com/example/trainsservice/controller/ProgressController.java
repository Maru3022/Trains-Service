package com.example.trainsservice.controller;

import com.example.trainsservice.dto.ProgressUpdateDTO;
import com.example.trainsservice.service.MovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/progress")
public class ProgressController {
    private final MovementService movementService;

    @PostMapping("/log")
    public ResponseEntity<String> logExercise(
            @RequestBody ProgressUpdateDTO dto
    ) {
        movementService.registerSet(dto);
        return ResponseEntity.ok("Workout progress has been successfully logged.");
    }
}
