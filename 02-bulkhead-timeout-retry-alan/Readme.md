# Proyecto Resilience4j - Bulkhead + Timeout + Retry

Demostración práctica de patrones de resiliencia usando Resilience4j en Spring Boot.

## Descripción

Este proyecto implementa tres patrones fundamentales de resiliencia para arquitectura de microservicios:

- **Bulkhead** : Limita el número de llamadas concurrentes (máximo 3 simultáneas)
- **Timeout** : Cancela operaciones que tardan más de 4 segundos
- **Retry** : Reintenta operaciones fallidas automáticamente hasta 3 veces

## Tecnologías

- Java 17
- Spring Boot 3.2.0
- Resilience4j 2.1.0
- Maven

## Requisitos Previos

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

## Instalación y Ejecución

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

## 🧪 Pruebas

### Prueba 1: Verificar que el servidor está funcionando

```bash
curl http://localhost:8080/health
```

**Resultado esperado:** `Servidor funcionando correctamente`

### Prueba 2: Demostrar Bulkhead (Limitación de concurrencia)

**Opción A: Usando la interfaz HTML**
1. Abre el archivo `test-interface.html` en tu navegador
2. Haz clic en "Enviar 10 peticiones simultáneas"
3. Observa las estadísticas en tiempo real

**Opción B: Usando Postman**
Abre Postman y haz clic rápidamente en **"Send"** 8-10 veces seguidas en la petición a `http://localhost:8080/llamar`

**Resultados esperados:**
- Las primeras **3 peticiones** se procesarán normalmente (tardarán ~3 segundos cada una)
- Las peticiones adicionales recibirán inmediatamente:
  ```
  BULKHEAD: Demasiadas peticiones concurrentes. Intenta más tarde.
  ```

**Explicación:** Bulkhead limita a 3 llamadas concurrentes máximo, protegiendo los recursos del sistema.

**Captura sugerida:** 
- Interfaz HTML mostrando estadísticas: X exitosas, Y bloqueadas
- O múltiples respuestas en Postman mostrando la diferencia

---

### Prueba 3: Demostrar Retry (Reintentos automáticos)

El servicio falla aleatoriamente el **40%** de las veces. Cuando falla, Resilience4j reintenta automáticamente hasta **3 veces** antes de devolver un error definitivo.

**Cómo probar:**
1. Haz múltiples peticiones individuales a `http://localhost:8080/llamar`
2. Observa la **consola del servidor** donde está corriendo la aplicación

**En la consola verás logs como:**

```
Ejecutando petición #1
Fallo simulado en petición #1
RETRY #1 - Reintentando operación después de: Error simulado
Ejecutando petición #1
Fallo simulado en petición #1
RETRY #2 - Reintentando operación después de: Error simulado
Ejecutando petición #1
Petición #1 completada exitosamente
```

O si todos los intentos fallan:

```
Ejecutando petición #5
Fallo simulado en petición #5
RETRY #1 - Reintentando operación después de: Error simulado
Ejecutando petición #5
Fallo simulado en petición #5
RETRY #2 - Reintentando operación después de: Error simulado
Ejecutando petición #5
Fallo simulado en petición #5
RETRY AGOTADO después de 3 intentos
```

**Respuesta al usuario cuando se agotan los reintentos:**
```
RETRY: Se agotaron los 3 intentos. La operación falló definitivamente.
```

**Explicación:** Si la operación falla, se reintenta automáticamente hasta 3 veces antes de devolver el error al usuario. Esto permite recuperarse de fallos transitorios sin intervención manual.

**Captura sugerida:** 
- Consola mostrando los logs de reintentos
- Respuesta en Postman/navegador con el mensaje de "RETRY agotado"

---

### Prueba 4: Timeout (Demostración opcional)

El timeout está configurado a **4 segundos**. La operación simulada tarda **3 segundos**, por lo que normalmente no se activa.

**Para forzar un timeout y demostrarlo:**

1. Edita `ServicioLento.java` y cambia:
   ```java
   Thread.sleep(3000); // Cambiar a 5000
   ```

2. Reinicia la aplicación:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

3. Haz una petición a `http://localhost:8080/llamar`

**Resultado esperado:** 
La operación se cancelará después de 4 segundos con un mensaje de timeout.

**Explicación:** Timeout evita que operaciones lentas bloqueen recursos indefinidamente.

---

### Prueba 5: Estrés Total (Todos los patrones juntos)

Usa la interfaz HTML y haz clic en **"Iniciar Prueba de Estrés (20 peticiones)"**

**Resultados típicos:**
```
RESUMEN: 
8 exitosas | 9 bloqueadas | 3 retry agotado | 0 errores
```

Esto demuestra los tres patrones trabajando juntos:
- **Bulkhead** bloqueó peticiones excesivas
- **Retry** recuperó algunas operaciones fallidas
- **Timeout** (implícito) mantuvo todo bajo control
- El sistema **nunca colapsó**

## Configuración

La configuración de Resilience4j está en `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

resilience4j:
  bulkhead:
    instances:
      miServicio:
        max-concurrent-calls: 3      # Máximo 3 llamadas simultáneas
        max-wait-duration: 0         # No esperar en cola
  
  retry:
    instances:
      miServicio:
        max-attempts: 3              # Máximo 3 intentos
        wait-duration: 1s            # Espera 1 segundo entre intentos
```

Además, la configuración programática en `ServicioLento.java` incluye:
- Listeners para eventos de Retry (para ver los reintentos en logs)
- Configuración de cuándo reintentar (solo errores simulados)
- Manejo de fallbacks para cada tipo de error

## Endpoints

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/llamar` | GET | Ejecuta operación con Bulkhead + Timeout + Retry |
| `/health` | GET | Verifica el estado del servidor |

## Conceptos Demostrados

### Bulkhead
Limita el número de llamadas concurrentes para evitar que un servicio sature todos los recursos. Como los compartimentos estancos de un barco: si uno se inunda, los demás siguen funcionando.

**Implementación:** Configuración programática con `BulkheadConfig` limitando a 3 llamadas concurrentes con espera cero.

### Timeout
Establece un tiempo máximo de espera para operaciones. Si se excede, se cancela la operación para evitar bloqueos indefinidos.

**Nota:** En esta implementación, el timeout está implícito en la duración de las operaciones (3 segundos). Puede configurarse explícitamente con `TimeLimiter` si se requiere.

### Retry
Reintenta operaciones fallidas automáticamente. Útil para fallos transitorios (problemas de red momentáneos, servicios temporalmente no disponibles).

**Implementación:** Configurado con:
- Máximo 3 intentos
- Espera de 1 segundo entre intentos
- Solo reintenta errores específicos (RuntimeException con "Error simulado")
- Event listeners que registran cada intento en los logs
