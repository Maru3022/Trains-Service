package com.example.trainsservice.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {

    @Test
    void prePersist_setsDefaultsWhenNull() {
        OutboxEvent e = new OutboxEvent();
        e.prePersist();
        assertNotNull(e.getId());
        assertNotNull(e.getCreatedAt());
        assertEquals(OutboxEvent.Status.PENDING, e.getStatus());
        assertEquals(0, e.getRetryCount());
        assertEquals(3, e.getMaxRetries());
    }

    @Test
    void prePersist_keepsExistingValues() {
        OutboxEvent e = new OutboxEvent();
        e.setId(null);
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setStatus(OutboxEvent.Status.SENT);
        e.setRetryCount(5);
        e.setMaxRetries(10);
        e.prePersist();
        assertNotNull(e.getId());
        assertEquals(now, e.getCreatedAt());
        assertEquals(OutboxEvent.Status.SENT, e.getStatus());
        assertEquals(5, e.getRetryCount());
        assertEquals(10, e.getMaxRetries());
    }
}
