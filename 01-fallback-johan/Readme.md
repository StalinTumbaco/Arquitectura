# Circuit Breaker y Fallback - Demo Spring Boot

## 📋 Descripción

Este proyecto demuestra la implementación de los patrones **Circuit Breaker** y **Fallback** en un microservicio Spring Boot. El servicio simula llamadas a un endpoint externo que puede fallar, y cuando esto ocurre, implementa una estrategia de fallback de dos niveles: primero intenta usar datos en caché, y si no hay caché disponible, devuelve una respuesta estática predefinida.

## 🎯 Objetivos

- Demostrar el patrón **Circuit Breaker** usando Resilience4j
- Implementar **Fallback local** con estrategia de dos niveles (caché + estático)
- Simular un servicio externo que falla aleatoriamente
- Mostrar cómo el sistema se protege automáticamente ante fallos

## 🏗️ Arquitectura

```
┌─────────────────┐
│  DemoController │
│   /api/demo     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐      ┌──────────────────┐
│ExternalApiService│─────▶│ Circuit Breaker   │
│  (con @Circuit   │      │  (Resilience4j)   │
│   Breaker)       │      └─────────┬─────────┘
└────────┬────────┘                 │
         │                          │
         │                          ▼
         │                  ┌──────────────────┐
         │                  │ External Service │
         │                  │  (Mock - 50%     │
         │                  │   probabilidad   │
         │                  │   de fallar)     │
         │                  └──────────────────┘
         │
         │ (si falla)
         ▼
┌─────────────────┐
│ FallbackService │
│  Estrategia:    │
│  1. Caché       │
│  2. Estático    │
└─────────────────┘
```

## 📦 Tecnologías Utilizadas

- **Spring Boot 3.2.0**: Framework principal
- **Resilience4j 2.1.0**: Librería para Circuit Breaker
- **Spring Cache**: Para implementar el fallback con caché
- **Spring Actuator**: Para monitoreo y métricas
- **Maven**: Gestor de dependencias

## 🚀 Cómo Ejecutar

### Prerrequisitos

- Java 17 o superior
- Maven 3.6 o superior

### Pasos

1. **Compilar el proyecto:**
   ```bash
   mvn clean install
   ```

2. **Ejecutar la aplicación:**
   ```bash
   mvn spring-boot:run
   ```

3. **Verificar que la aplicación está corriendo:**
   - La aplicación se iniciará en `http://localhost:8081`
   - Puedes verificar el health check en: `http://localhost:8081/actuator/health`

## 🧪 Endpoints Disponibles

### 1. Endpoint de Demostración Principal

**GET** `/api/demo/data`

Llama al servicio externo y devuelve los datos. Si el servicio falla o el Circuit Breaker está abierto, devuelve datos del fallback.

**Ejemplo de respuesta exitosa:**
```json
{
  "id": 1,
  "message": "Datos del servicio externo",
  "timestamp": "2024-01-15T10:30:00",
  "data": {
    "value1": "Información importante",
    "value2": 12345,
    "status": "active"
  },
  "_metadata": {
    "circuitBreakerState": "CLOSED",
    "failureRate": 0.0,
    "numberOfSuccessfulCalls": 5,
    "numberOfFailedCalls": 0
  }
}
```

**Ejemplo de respuesta con fallback:**
```json
{
  "source": "FALLBACK_STATIC",
  "message": "Datos de respaldo (fallback estático)",
  "timestamp": "2024-01-15T10:30:00",
  "data": {
    "value1": "Información de respaldo",
    "value2": 99999,
    "status": "fallback"
  },
  "note": "Este es un fallback estático porque el servicio externo no está disponible",
  "_metadata": {
    "circuitBreakerState": "OPEN",
    "failureRate": 0.6,
    "numberOfSuccessfulCalls": 2,
    "numberOfFailedCalls": 3
  }
}
```

### 2. Estado del Circuit Breaker

**GET** `/api/demo/circuit-breaker/status`

Devuelve el estado actual del Circuit Breaker y sus métricas.

**Ejemplo de respuesta:**
```json
{
  "state": "CLOSED",
  "failureRate": 0.0,
  "numberOfSuccessfulCalls": 10,
  "numberOfFailedCalls": 0,
  "numberOfNotPermittedCalls": 0,
  "numberOfBufferedCalls": 10
}
```

### 3. Resetear Circuit Breaker

**GET** `/api/demo/circuit-breaker/reset`

Resetea manualmente el Circuit Breaker al estado CLOSED.

### 4. Servicio Mock Externo

**GET** `/external/data`

Simula un servicio externo que falla aleatoriamente (50% de probabilidad).

**GET** `/external/health`

Verifica el estado del servicio mock.

## ⚙️ Configuración del Circuit Breaker

La configuración se encuentra en `application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      externalService:
        slidingWindowSize: 10              # Ventana deslizante de 10 llamadas
        minimumNumberOfCalls: 5            # Mínimo 5 llamadas antes de evaluar
        failureRateThreshold: 50           # Abre circuito si 50% fallan
        waitDurationInOpenState: 5s        # Espera 5 segundos antes de intentar de nuevo
        automaticTransitionFromOpenToHalfOpenEnabled: true
        permittedNumberOfCallsInHalfOpenState: 3
```

### Estados del Circuit Breaker

1. **CLOSED (Cerrado)**: Estado normal, las llamadas pasan normalmente
2. **OPEN (Abierto)**: El circuito está abierto, todas las llamadas van directo al fallback
3. **HALF_OPEN (Semi-abierto)**: Estado de prueba, permite algunas llamadas para verificar si el servicio se recuperó

### Transiciones

- **CLOSED → OPEN**: Cuando la tasa de fallos supera el `failureRateThreshold` (50%)
- **OPEN → HALF_OPEN**: Después de `waitDurationInOpenState` (5 segundos)
- **HALF_OPEN → CLOSED**: Si las llamadas de prueba son exitosas
- **HALF_OPEN → OPEN**: Si las llamadas de prueba fallan

## 🔄 Estrategia de Fallback

El sistema implementa una estrategia de fallback de **dos niveles**:

### Nivel 1: Caché
- Si hay datos exitosos previos almacenados en caché, se utilizan
- Los datos se guardan automáticamente cuando una llamada al servicio externo es exitosa
- Caché implementado con Spring Cache (Simple Cache)

### Nivel 2: Respuesta Estática
- Si no hay datos en caché, se devuelve una respuesta estática predefinida
- Esta respuesta garantiza que el sistema siempre tenga una respuesta, incluso si nunca ha tenido una llamada exitosa

### Flujo de Fallback

```
Llamada al servicio externo
         │
         ▼
    ¿Éxito?
    │      │
   SÍ      NO
    │      │
    │      ▼
    │  Circuit Breaker
    │      │
    │      ▼
    │  ¿Estado OPEN?
    │      │
    │     SÍ ──────────┐
    │      │            │
    │      NO           │
    │      │            │
    │      ▼            │
    │  Ejecutar Fallback│
    │      │            │
    │      ▼            │
    │  ¿Hay caché?      │
    │      │            │
    │     SÍ ──► Usar caché
    │      │            │
    │      NO           │
    │      │            │
    │      ▼            │
    └──► Respuesta estática
```

## 📊 Monitoreo

### Actuator Endpoints

- **Health**: `http://localhost:8081/actuator/health`
- **Metrics**: `http://localhost:8081/actuator/metrics`
- **Circuit Breakers**: `http://localhost:8081/actuator/circuitbreakers`

### Logs

El proyecto incluye logging detallado:
- `INFO`: Operaciones normales
- `WARN`: Cuando se ejecuta fallback
- `ERROR`: Errores al llamar al servicio externo
- `DEBUG`: Para Resilience4j (configurado en `application.yml`)

## 🧩 Componentes Principales

### 1. `ExternalServiceController`
Controlador mock que simula un servicio externo con fallos aleatorios (50% de probabilidad).

### 2. `ExternalApiService`
Servicio que llama al endpoint externo con protección de Circuit Breaker. Usa la anotación `@CircuitBreaker` de Resilience4j.

### 3. `FallbackService`
Implementa la lógica de fallback de dos niveles (caché + estático).

### 4. `DemoController`
Controlador REST que expone los endpoints de demostración y permite consultar el estado del Circuit Breaker.

## 🎓 Conceptos Demostrados

### Circuit Breaker Pattern
Patrón que previene llamadas repetidas a un servicio que está fallando, permitiendo que el sistema se recupere.

### Fallback Pattern
Patrón que proporciona una respuesta alternativa cuando el servicio principal no está disponible.

### Caching Strategy
Uso de caché para almacenar respuestas exitosas y reutilizarlas en caso de fallo.

## 🔍 Pruebas

### Probar el Circuit Breaker

1. **Hacer múltiples llamadas al endpoint de demo:**
   ```bash
   curl http://localhost:8081/api/demo/data
   ```

2. **Observar cómo el Circuit Breaker cambia de estado:**
   ```bash
   curl http://localhost:8081/api/demo/circuit-breaker/status
   ```

3. **Después de varios fallos, el circuito se abrirá y todas las llamadas irán directo al fallback**

4. **Esperar 5 segundos y el circuito intentará recuperarse (HALF_OPEN)**

### Escenarios de Prueba

- **Servicio estable**: Hacer 10+ llamadas exitosas, verificar que el Circuit Breaker permanece CLOSED
- **Servicio inestable**: Hacer llamadas hasta que el Circuit Breaker se abra (OPEN)
- **Recuperación**: Esperar y verificar la transición a HALF_OPEN y luego a CLOSED si hay éxito

## 📝 Notas Importantes

- El servicio mock falla aleatoriamente con 50% de probabilidad
- El Circuit Breaker necesita al menos 5 llamadas antes de evaluar si debe abrirse
- El tiempo de espera en estado OPEN es de 5 segundos
- Los datos en caché se mantienen durante la ejecución de la aplicación
- El fallback estático siempre está disponible como último recurso

## 🛠️ Personalización

Puedes ajustar la configuración del Circuit Breaker en `application.yml`:

- `slidingWindowSize`: Tamaño de la ventana de evaluación
- `minimumNumberOfCalls`: Número mínimo de llamadas antes de evaluar
- `failureRateThreshold`: Porcentaje de fallos que activa el circuito
- `waitDurationInOpenState`: Tiempo de espera antes de intentar recuperación

## 📚 Referencias

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)

## 👤 Autor

**Johan** - Implementación de Fallback + Circuit Breaker simple

---

**Nota**: Este proyecto es independiente y no requiere dependencias de otros módulos (Alan ni Stalin).
