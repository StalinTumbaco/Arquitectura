package com.fallback.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Controlador mock que simula un servicio externo.
 * Simula fallos aleatorios para demostrar el Circuit Breaker.
 */
@RestController
@RequestMapping("/external")
public class ExternalServiceController {

    private final Random random = new Random();
    private int requestCount = 0;

    /**
     * Endpoint que simula un servicio externo con fallos aleatorios.
     * Tiene un 50% de probabilidad de fallar.
     * 
     * @return Respuesta exitosa o error 500
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getExternalData() {
        requestCount++;
        
        // Simula fallo aleatorio (50% de probabilidad)
        if (random.nextBoolean()) {
            // Simula timeout o error del servicio externo
            try {
                Thread.sleep(100); // Simula latencia
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Servicio externo no disponible"));
        }

        // Respuesta exitosa
        Map<String, Object> response = new HashMap<>();
        response.put("id", requestCount);
        response.put("message", "Datos del servicio externo");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("data", Map.of(
            "value1", "Información importante",
            "value2", 12345,
            "status", "active"
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar el estado del servicio mock.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Mock External Service");
        health.put("totalRequests", requestCount);
        return ResponseEntity.ok(health);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now().toString());
        return error;
    }
}

