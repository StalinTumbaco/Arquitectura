package com.fallback.demo.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio que llama al endpoint externo con Circuit Breaker.
 * Cuando el circuito está abierto o hay un fallo, usa el FallbackService.
 */
@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    private static final String CIRCUIT_BREAKER_NAME = "externalService";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FallbackService fallbackService;

    @Value("${external.service.url:http://localhost:8081/external/data}")
    private String externalServiceUrl;

    /**
     * Llama al servicio externo con protección de Circuit Breaker.
     * Si falla, automáticamente ejecuta el método de fallback.
     * 
     * @return Datos del servicio externo o del fallback
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallback")
    public Map<String, Object> getExternalData() {
        logger.info("Intentando llamar al servicio externo: {}", externalServiceUrl);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                externalServiceUrl, 
                Map.class
            );
            
            // Si la respuesta es exitosa, guardar en caché para uso futuro
            if (response != null && !isErrorResponse(response)) {
                saveToCache("lastSuccessfulResponse", response);
                logger.info("Respuesta exitosa del servicio externo");
            }
            
            return response;
        } catch (Exception e) {
            logger.error("Error al llamar al servicio externo: {}", e.getMessage());
            throw new RuntimeException("Error al llamar al servicio externo", e);
        }
    }

    /**
     * Método de fallback que se ejecuta cuando:
     * - El Circuit Breaker está abierto
     * - Hay una excepción al llamar al servicio externo
     * 
     * @param exception La excepción que causó el fallback (puede ser null si el circuito está abierto)
     * @return Datos del fallback (caché o estático)
     */
    public Map<String, Object> fallback(Exception exception) {
        String reason = exception != null ? exception.getMessage() : "Circuit Breaker está abierto";
        logger.warn("Ejecutando fallback debido a: {}", reason);
        
        // Estrategia de fallback de dos niveles:
        // 1. Intentar obtener de caché
        Map<String, Object> cachedData = fallbackService.getCachedData("lastSuccessfulResponse");
        if (cachedData != null) {
            logger.info("Usando datos de caché para fallback");
            return cachedData;
        }
        
        // 2. Si no hay caché, usar respuesta estática
        logger.info("Usando respuesta estática de fallback");
        return fallbackService.getFallbackData("static");
    }

    /**
     * Guarda datos en caché para uso futuro en fallback.
     * 
     * @param key Clave para almacenar
     * @param data Datos a almacenar
     */
    @CachePut(value = "fallbackCache", key = "#key")
    private Map<String, Object> saveToCache(String key, Map<String, Object> data) {
        return data;
    }

    /**
     * Verifica si la respuesta es un error.
     * 
     * @param response Respuesta a verificar
     * @return true si es un error
     */
    private boolean isErrorResponse(Map<String, Object> response) {
        return response.containsKey("error") && 
               Boolean.TRUE.equals(response.get("error"));
    }
}

