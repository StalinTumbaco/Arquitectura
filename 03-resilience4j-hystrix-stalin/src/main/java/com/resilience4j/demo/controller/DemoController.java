package com.resilience4j.demo.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    /**
     * Endpoint que responde inmediatamente con un mensaje simple
     */
    @GetMapping("/ok")
    public Map<String, String> ok() {
        logger.info("Endpoint /api/ok llamado - Respondiendo exitosamente");
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Servicio funcionando correctamente");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    /**
     * Endpoint lento protegido por CircuitBreaker
     * Simula un servicio lento o lanza un error según el parámetro
     * 
     * @param shouldFail si es true, lanza una excepción; si es false, simula delay
     */
    @GetMapping("/lento")
    @CircuitBreaker(name = "demoCircuitBreaker", fallbackMethod = "fallback")
    public Map<String, String> lento(@RequestParam(defaultValue = "false") boolean shouldFail) throws Exception {
        logger.info("Endpoint /api/lento llamado - shouldFail: {}", shouldFail);
        
        if (shouldFail) {
            logger.error("Simulando un error en el servicio");
            throw new RuntimeException("Error simulado en el servicio");
        }
        
        // Simular un servicio lento
        logger.info("Simulando servicio lento (5 segundos)...");
        Thread.sleep(5000);
        
        logger.info("Servicio lento completado exitosamente");
        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Servicio lento completado después de 5 segundos");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    /**
     * Método fallback que se ejecuta cuando el CircuitBreaker está OPEN
     * o cuando ocurre un error en el método principal
     * 
     * @param exception la excepción que causó el fallback (puede ser null)
     */
    public Map<String, String> fallback(Exception exception) {
        logger.warn("FALLBACK ACTIVADO - Razón: {}", 
            exception != null ? exception.getMessage() : "CircuitBreaker OPEN");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "FALLBACK");
        response.put("message", "Fallback activado por error o timeout");
        response.put("reason", exception != null ? exception.getClass().getSimpleName() : "Circuit Open");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }
}
