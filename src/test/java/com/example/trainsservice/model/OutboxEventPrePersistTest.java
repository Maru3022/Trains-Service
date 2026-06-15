package com.example.trainsservice.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventPrePersistTest {

    @Test
    void prePersist_setsDefaults() {
        OutboxEvent e = new OutboxEvent();
        assertNull(e.getId());
        assertNull(e.getCreatedAt());
        assertNull(e.getStatus());
        assertNull(e.getRetryCount());
        assertNull(e.getMaxRetries());

        e.prePersist();

        assertNotNull(e.getId());
        assertNotNull(e.getCreatedAt());
        assertTrue(e.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertEquals(OutboxEvent.Status.PENDING, e.getStatus());
        assertEquals(0, e.getRetryCount());
        assertEquals(3, e.getMaxRetries());
    }
}
