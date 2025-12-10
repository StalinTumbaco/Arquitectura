package com.ejemplo.resiliencedemo;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class ServicioLento {
    
    private Random random = new Random();
    private int contador = 0;

    @Bulkhead(name = "miServicio", fallbackMethod = "fallbackBulkhead")
    @TimeLimiter(name = "miServicio")
    @Retry(name = "miServicio", fallbackMethod = "fallbackRetry")
    public CompletableFuture<String> operacionLenta() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                contador++;
                System.out.println("Ejecutando petición #" + contador);
                
                Thread.sleep(3000);
                
                if (random.nextInt(100) < 30) {
                    throw new RuntimeException("Error simulado");
                }
                
                return "Operación exitosa #" + contador;
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrumpido", e);
            }
        });
    }

    private CompletableFuture<String> fallbackBulkhead(Exception e) {
        return CompletableFuture.completedFuture(
            "BULKHEAD: Demasiadas peticiones concurrentes. Intenta más tarde."
        );
    }

    private CompletableFuture<String> fallbackRetry(Exception e) {
        return CompletableFuture.completedFuture(
            "RETRY: Se agotaron los intentos. Error: " + e.getMessage()
        );
    }
}