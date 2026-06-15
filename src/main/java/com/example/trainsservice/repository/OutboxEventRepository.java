package com.example.trainsservice.repository;

import com.example.trainsservice.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxEvent> findByStatusOrderByCreatedAt(@Param("status") OutboxEvent.Status status);

    @Query(value = "SELECT * FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at ASC FOR UPDATE SKIP LOCKED LIMIT :batchSize", nativeQuery = true)
    List<OutboxEvent> fetchNextPendingEventsForProcessing(@Param("batchSize") int batchSize);

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = com.example.trainsservice.model.OutboxEvent.Status.PROCESSING AND o.processedAt < :cutoff")
    List<OutboxEvent> findStuckProcessingEvents(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEvent o SET o.status = com.example.trainsservice.model.OutboxEvent.Status.PROCESSING, o.processedAt = :processedAt WHERE o.id IN :ids")
    int markProcessing(@Param("ids") List<UUID> ids, @Param("processedAt") LocalDateTime processedAt);
}