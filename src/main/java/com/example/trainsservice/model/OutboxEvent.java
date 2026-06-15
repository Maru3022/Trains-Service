package com.example.trainsservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_events_status_created_at", columnList = "status, created_at"),
        @Index(name = "idx_outbox_events_aggregate_id", columnList = "aggregate_id")
})
@Getter
@Setter
public class OutboxEvent {

    public enum Status {
        PENDING,
        PROCESSING,
        SENT,
        FAILED,
        DEAD_LETTER
    }

    @Id
    private UUID id;

    @Column(name = "aggregate_type", length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(nullable = false, length = 255)
    private String topic;

    @Column(name = "`key`", length = 255)
    private String key;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (maxRetries == null) {
            maxRetries = 3;
        }
    }
}