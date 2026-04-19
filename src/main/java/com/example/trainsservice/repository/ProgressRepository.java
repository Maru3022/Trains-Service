package com.example.trainsservice.repository;

import com.example.trainsservice.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    @Query("select coalesce(sum(coalesce(p.reps, 0) * coalesce(p.weight, 0.0)), 0.0) from Progress p")
    double sumVolume();
}
