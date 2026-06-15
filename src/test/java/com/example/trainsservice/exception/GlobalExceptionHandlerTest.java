package com.example.trainsservice.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEntityNotFound_returns404() {
        EntityNotFoundException ex = new EntityNotFoundException("not found");
        ResponseEntity<String> resp = handler.handleEntityNotFoundException(ex);
        assertEquals(404, resp.getStatusCodeValue());
        assertEquals("not found", resp.getBody());
    }

    @Test
    void handleGeneralException_returns500() {
        Exception ex = new Exception("oops");
        ResponseEntity<String> resp = handler.handleGeneralException(ex);
        assertEquals(500, resp.getStatusCodeValue());
        assertEquals("An unexpected error occurred.", resp.getBody());
    }
}
