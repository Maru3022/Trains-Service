package com.example.trainsservice.repository;

import com.example.trainsservice.model.Stats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsRepository
        extends JpaRepository<Stats,Long> {
}
