package com.example.trainsservice.repository;

import com.example.trainsservice.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    @Query("select coalesce(sum(coalesce(p.reps, 0) * coalesce(p.weight, 0.0)), 0.0) from Progress p")
    double sumVolume();

    long countByTrainUserId(String userId);

    @Query("select coalesce(sum(coalesce(p.reps, 0) * coalesce(p.weight, 0.0)), 0.0) from Progress p where p.train.userId = :userId")
    double sumVolumeByUserId(@Param("userId") String userId);
}
