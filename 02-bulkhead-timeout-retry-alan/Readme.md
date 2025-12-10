# Proyecto Resilience4j - Bulkhead + Timeout + Retry

Demostración de patrones de resiliencia usando Resilience4j en Spring Boot.

##  Descripción

Este proyecto implementa tres patrones de resiliencia para arquitectura de software:

- **Bulkhead**: Limita el número de llamadas concurrentes (máximo 3)
- **Timeout**: Cancela operaciones que tardan más de 4 segundos
- **Retry**: Reintenta operaciones fallidas hasta 3 veces

##  Tecnologías

- Java 17
- Spring Boot 3.2.0
- Resilience4j 2.1.0
- Maven

##  Requisitos Previos

- **JDK 17 o superior**: [Descargar aquí](https://adoptium.net/)
- Maven (incluido en el proyecto como wrapper)

### Verificar Java instalado

```bash
java -version
```

Debería mostrar Java 17 o superior.

### Configurar JAVA_HOME (si es necesario)

**Windows:**
1. Panel de Control → Sistema → Configuración avanzada del sistema
2. Variables de entorno → Nueva variable de usuario
3. Nombre: `JAVA_HOME`
4. Valor: Ruta de instalación de Java (ej: `C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot`)

##  Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd resilience-demo
```

### 2. Ejecutar el proyecto

**Windows:**
```bash
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

La aplicación se ejecutará en: `http://localhost:8080`

##  Pruebas

### Prueba 1: Verificar que el servidor está funcionando

```bash
curl http://localhost:8080/health
```

**Resultado esperado:** `Servidor funcionando correctamente`

### Prueba 2: Demostrar Bulkhead (Limitación de concurrencia)

Abre **5-6 pestañas** del navegador simultáneamente y visita:
```
http://localhost:8080/llamar
```

**Resultados esperados:**
- Las primeras **3 peticiones** se procesarán normalmente (tardarán 3 segundos)
- Las peticiones **4, 5, 6...** recibirán: 
  ```
  BULKHEAD: Demasiadas peticiones concurrentes. Intenta más tarde.
  ```

**Explicación:** Bulkhead limita a 3 llamadas concurrentes máximo.

### Prueba 3: Demostrar Timeout

El timeout está configurado a **4 segundos**. Si el servicio tarda más, se cancela.

La operación está simulada para tardar **3 segundos**, por lo que normalmente no se activará el timeout.

**Para forzar un timeout**, modifica en `ServicioLento.java`:
```java
Thread.sleep(5000); // Cambiar de 3000 a 5000
```

Reinicia y visita `http://localhost:8080/llamar`

**Resultado esperado:** Error de timeout después de 4 segundos.

### Prueba 4: Demostrar Retry (Reintentos)

El servicio falla aleatoriamente el **30%** de las veces. Cuando falla, Resilience4j reintenta automáticamente hasta **3 veces**.

Haz múltiples peticiones a:
```
http://localhost:8080/llamar
```

**En la consola del servidor** verás:
```
Ejecutando petición #1
Ejecutando petición #2
Ejecutando petición #2  (reintento)
Ejecutando petición #2  (reintento)
RETRY: Se agotaron los intentos. Error: Error simulado
```

**Explicación:** Si la operación falla, se reintenta automáticamente hasta 3 veces antes de devolver el error al usuario.

##  Configuración

La configuración de Resilience4j está en `src/main/resources/application.yml`:

```yaml
resilience4j:
  bulkhead:
    instances:
      miServicio:
        max-concurrent-calls: 3      # Máximo 3 llamadas simultáneas
        max-wait-duration: 1s        # Espera máxima en cola
  
  timelimiter:
    instances:
      miServicio:
        timeout-duration: 4s         # Timeout de 4 segundos
  
  retry:
    instances:
      miServicio:
        max-attempts: 3              # Máximo 3 intentos
        wait-duration: 1s            # Espera 1 segundo entre intentos
```

##  Endpoints

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/llamar` | GET | Ejecuta operación con Bulkhead + Timeout + Retry |
| `/health` | GET | Verifica el estado del servidor |

##  Conceptos Demostrados

### Bulkhead
Limita el número de llamadas concurrentes para evitar que un servicio sature todos los recursos. Como los compartimentos estancos de un barco, si uno se inunda, los demás siguen funcionando.

### Timeout
Establece un tiempo máximo de espera para operaciones. Si se excede, se cancela la operación para evitar bloqueos indefinidos.

### Retry
Reintenta operaciones fallidas automáticamente. Útil para fallos transitorios (problemas de red momentáneos, servicios temporalmente no disponibles).
