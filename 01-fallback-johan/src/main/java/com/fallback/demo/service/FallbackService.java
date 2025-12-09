package com.fallback.demo.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio que implementa la lógica de fallback.
 * Estrategia de dos niveles:
 * 1. Intenta usar datos en caché (si existen)
 * 2. Si no hay caché, devuelve respuesta estática predefinida
 */
@Service
public class FallbackService {

    /**
     * Obtiene datos del fallback.
     * Primero intenta obtener de caché, si no existe devuelve respuesta estática.
     * 
     * @param key Clave para buscar en caché
     * @return Datos de fallback
     */
    @Cacheable(value = "fallbackCache", key = "#key")
    public Map<String, Object> getFallbackData(String key) {
        // Si no está en caché, devuelve respuesta estática
        return getStaticFallbackData();
    }

    /**
     * Guarda datos en caché para uso futuro.
     * 
     * @param key Clave para almacenar
     * @param data Datos a almacenar
     */
    public void saveToCache(String key, Map<String, Object> data) {
        // El caché se maneja automáticamente con @Cacheable
        // Este método puede ser usado para guardar datos explícitamente
    }

    /**
     * Obtiene respuesta estática predefinida cuando no hay caché disponible.
     * 
     * @return Datos estáticos de fallback
     */
    private Map<String, Object> getStaticFallbackData() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("source", "FALLBACK_STATIC");
        fallback.put("message", "Datos de respaldo (fallback estático)");
        fallback.put("timestamp", LocalDateTime.now().toString());
        fallback.put("data", Map.of(
            "value1", "Información de respaldo",
            "value2", 99999,
            "status", "fallback"
        ));
        fallback.put("note", "Este es un fallback estático porque el servicio externo no está disponible");
        return fallback;
    }

    /**
     * Obtiene datos de fallback desde caché si existen.
     * 
     * @param key Clave para buscar en caché
     * @return Datos de caché o null si no existen
     */
    public Map<String, Object> getCachedData(String key) {
        // Este método puede ser usado para verificar si hay datos en caché
        // La implementación real dependería del proveedor de caché
        return null; // Simplificado para esta demo
    }
}

