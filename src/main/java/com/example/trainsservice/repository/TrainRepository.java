package com.example.trainsservice.repository;

import com.example.trainsservice.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByUserIdAndCategory(String userId, String category);
    Optional<Train> findByIdAndUserId(Long id, String userId);
    List<Train> findAllByUserId(String userId);
}
