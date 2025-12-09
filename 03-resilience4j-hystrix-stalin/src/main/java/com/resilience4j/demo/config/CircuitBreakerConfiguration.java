package com.resilience4j.demo.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerConfiguration.class);

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Registra listeners para los eventos del CircuitBreaker
     * Muestra en logs cuando el circuito cambia de estado
     */
    @PostConstruct
    public void registerEventListeners() {
        if (circuitBreakerRegistry != null) {
            circuitBreakerRegistry.circuitBreaker("demoCircuitBreaker")
                .getEventPublisher()
                .onStateTransition(event -> {
                    logger.warn("╔═══════════════════════════════════════════════════════════════╗");
                    logger.warn("║ CAMBIO DE ESTADO DEL CIRCUITBREAKER                           ║");
                    logger.warn("╠═══════════════════════════════════════════════════════════════╣");
                    logger.warn("║ CircuitBreaker: demoCircuitBreaker                            ║");
                    logger.warn("║ Estado anterior: {}                                   ", event.getStateTransition().getFromState());
                    logger.warn("║ Estado nuevo: {}                                      ", event.getStateTransition().getToState());
                    logger.warn("╚═══════════════════════════════════════════════════════════════╝");
                })
                .onSuccess(event -> {
                    logger.info("✓ Llamada exitosa - Duración: {} ms", event.getElapsedDuration().toMillis());
                })
                .onError(event -> {
                    logger.error("✗ Llamada fallida - Error: {}", event.getThrowable().getMessage());
                })
                .onCallNotPermitted(event -> {
                    logger.warn("⊘ Llamada NO PERMITIDA - CircuitBreaker está OPEN");
                });
            
            logger.info("Event listeners registrados para demoCircuitBreaker");
        }
    }
}
