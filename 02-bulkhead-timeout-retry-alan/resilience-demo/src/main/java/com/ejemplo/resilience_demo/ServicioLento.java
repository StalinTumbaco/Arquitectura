package com.ejemplo.resiliencedemo;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.function.Supplier;

@Service
public class ServicioLento {
    
    private Random random = new Random();
    private int contador = 0;
    private final Bulkhead bulkhead;
    private final Retry retry;

    public ServicioLento() {
        // Configurar Bulkhead
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(3)
                .maxWaitDuration(Duration.ZERO)
                .build();
        
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        this.bulkhead = bulkheadRegistry.bulkhead("miServicio");
        
        // Configurar Retry
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryOnException(e -> e instanceof RuntimeException && e.getMessage().contains("Error simulado"))
                .build();
        
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        this.retry = retryRegistry.retry("miServicio");
        
        // Agregar listener para ver los reintentos
        retry.getEventPublisher()
            .onRetry(event -> System.out.println("🔄 RETRY #" + event.getNumberOfRetryAttempts() + 
                " - Reintentando operación después de: " + event.getLastThrowable().getMessage()));
        
        retry.getEventPublisher()
            .onError(event -> System.out.println("❌ RETRY AGOTADO después de " + 
                event.getNumberOfRetryAttempts() + " intentos"));
    }

    public String operacionLenta() {
        // Decorar la operación con Bulkhead y Retry
        Supplier<String> decoratedSupplier = Bulkhead.decorateSupplier(bulkhead, 
            Retry.decorateSupplier(retry, this::ejecutarOperacion));
        
        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            return manejarError(e);
        }
    }

    private String ejecutarOperacion() {
        try {
            contador++;
            int numeroOperacion = contador;
            System.out.println("▶️  Ejecutando petición #" + numeroOperacion);
            
            // Simula operación lenta
            Thread.sleep(3000);
            
            // Falla aleatoriamente el 40% de las veces (aumentado para ver más reintentos)
            if (random.nextInt(100) < 40) {
                System.out.println("💥 Fallo simulado en petición #" + numeroOperacion);
                throw new RuntimeException("Error simulado");
            }
            
            System.out.println("✅ Petición #" + numeroOperacion + " completada exitosamente");
            return "✅ Operación exitosa #" + numeroOperacion;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrumpido", e);
        }
    }

    private String manejarError(Exception e) {
        String mensaje = e.getClass().getSimpleName() + ": " + e.getMessage();
        
        if (mensaje.contains("BulkheadFull")) {
            System.out.println("🚫 BULKHEAD ACTIVADO - Rechazando petición");
            return "🚫 BULKHEAD: Demasiadas peticiones concurrentes. Intenta más tarde.";
        } else if (mensaje.contains("Error simulado")) {
            System.out.println("🔄 RETRY FALLBACK - Todos los intentos fallaron");
            return "🔄 RETRY: Se agotaron los 3 intentos. La operación falló definitivamente.";
        } else {
            return "❌ ERROR: " + mensaje;
        }
    }
}