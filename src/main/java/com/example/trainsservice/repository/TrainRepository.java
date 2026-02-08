package com.example.trainsservice.repository;

import com.example.trainsservice.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository
        extends JpaRepository<Train, Long> {
}
