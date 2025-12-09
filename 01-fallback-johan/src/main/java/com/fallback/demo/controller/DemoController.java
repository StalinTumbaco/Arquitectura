package com.fallback.demo.controller;

import com.fallback.demo.service.ExternalApiService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para demostrar el funcionamiento del Circuit Breaker y Fallback.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @Autowired
    private ExternalApiService externalApiService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Endpoint principal de demostración.
     * Llama al servicio externo y muestra el resultado (o fallback si falla).
     * 
     * @return Respuesta con datos del servicio externo o del fallback
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getData() {
        Map<String, Object> response = externalApiService.getExternalData();
        
        // Agregar información del Circuit Breaker
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalService");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("circuitBreakerState", circuitBreaker.getState().toString());
        metadata.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
        metadata.put("numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        metadata.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
        
        response.put("_metadata", metadata);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener el estado del Circuit Breaker.
     * 
     * @return Estado actual del Circuit Breaker
     */
    @GetMapping("/circuit-breaker/status")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalService");
        
        Map<String, Object> status = new HashMap<>();
        status.put("state", circuitBreaker.getState().toString());
        status.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
        status.put("numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        status.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
        status.put("numberOfNotPermittedCalls", circuitBreaker.getMetrics().getNumberOfNotPermittedCalls());
        status.put("numberOfBufferedCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls());
        
        return ResponseEntity.ok(status);
    }

    /**
     * Endpoint para resetear el Circuit Breaker manualmente.
     * 
     * @return Confirmación del reset
     */
    @GetMapping("/circuit-breaker/reset")
    public ResponseEntity<Map<String, Object>> resetCircuitBreaker() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalService");
        circuitBreaker.transitionToClosedState();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Circuit Breaker reseteado a estado CLOSED");
        response.put("newState", circuitBreaker.getState().toString());
        
        return ResponseEntity.ok(response);
    }
}

