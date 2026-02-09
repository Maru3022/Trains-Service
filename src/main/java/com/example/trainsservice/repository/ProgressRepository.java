package com.example.trainsservice.repository;

import com.example.trainsservice.model.Progress;
import com.example.trainsservice.service.MovementService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository
        extends JpaRepository<Progress, Long> {
}
