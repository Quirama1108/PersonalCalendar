# Personal Calendar

Este proyecto es una pequeña aplicación de calendario personal desarrollada en Java como ejercicio técnico.

La idea principal es sencilla: poder agendar eventos, consultar lo que ya tengo ocupado y encontrar espacios disponibles en mi calendario.

Aunque la aplicación es pequeña, quise aprovecharla para trabajar conceptos que considero importantes en un proyecto backend: separación de responsabilidades, reglas de negocio claras, pruebas unitarias e integración, persistencia y medición de rendimiento.

## ¿Qué permite hacer?

Actualmente la aplicación permite:

* Crear eventos con título, descripción opcional, fecha y horario.
* Consultar eventos dentro de un rango de fechas.
* Actualizar eventos.
* Eliminar eventos (con confirmación desde la interfaz).
* Consultar espacios disponibles para una fecha.
* Detectar conflictos entre eventos.

También incluye una interfaz gráfica web (vistas de mes, semana y día, búsqueda, formularios de creación y edición) para usar el calendario sin depender de Postman o PowerShell.

> La documentación de calidad de software (SonarCloud, JaCoCo, complejidad, Quality Gate y Quality Profile) está en [`docs/calidad/`](docs/calidad/).

## Reglas del calendario

El calendario tiene unas reglas bastante simples:

| Día             | Horario disponible           |
| --------------- | ---------------------------- |
| Lunes a viernes | 09:00 - 17:00                |
| Sábado          | 09:00 - 16:00                |
| Domingo         | No se pueden agendar eventos |

Además:

* Un evento siempre debe empezar antes de terminar.
* No se puede crear un evento fuera del horario permitido.
* Dos eventos no pueden ocupar el mismo espacio de tiempo.
* Dos eventos consecutivos sí son válidos.

Por ejemplo:

```text
10:00 - 11:00
11:00 - 12:00
```

Esto no se considera un conflicto.

Internamente los intervalos se entienden como `[start, end)`: el inicio está incluido, pero el instante exacto del final ya puede ser utilizado por otro evento.

Tampoco obligué al calendario a trabajar únicamente con bloques de 15, 30 o 60 minutos. Un evento podría comenzar, por ejemplo, a las 09:17 si fuera necesario.

## Tecnologías utilizadas

El proyecto está desarrollado con:

* Java 21
* Gradle
* Spring Boot
* Spring Web
* Spring Data JPA
* H2
* JUnit 5
* MockMvc
* JMH
* HTML, CSS y JavaScript

## Cómo está organizado

Intenté mantener las reglas importantes alejadas de Spring, HTTP y la base de datos.

La estructura general es:

```text
HTTP / interfaz web
        ↓
Controllers
        ↓
Casos de uso
        ↓
Dominio
        ↓
EventRepository
        ↑
Adaptador JPA
        ↓
H2
```

La intención es que una regla como “los domingos no se puede agendar” no dependa de un controller o de una consulta SQL.

### Dominio

En `domain` están las clases que representan el problema principal.

`Event` representa un evento ya agendado. Tiene un identificador, título, inicio y fin.

`TimeSlot` representa simplemente un espacio de tiempo. Lo implementé como `record` porque no necesita una identidad propia: dos intervalos con el mismo inicio y fin representan el mismo valor.

`SchedulingPolicy` contiene las reglas del horario de trabajo.

`AvailabilityCalculator` recibe los eventos existentes y calcula qué espacios siguen disponibles.

### Casos de uso

La capa `application` contiene las acciones que puede realizar la aplicación:

```text
ScheduleEventUseCase
GetEventsUseCase
FindAvailabilityUseCase
UpdateEventUseCase
DeleteEventUseCase
```

Por ejemplo, para crear un evento no basta con guardarlo. `ScheduleEventUseCase`:

1. crea el evento;
2. valida el horario;
3. consulta los eventos existentes;
4. verifica si existe algún conflicto;
5. finalmente lo guarda.

La lógica de negocio queda en este flujo y el controller solamente se encarga de recibir y responder HTTP.

## Detección de conflictos

Para detectar si dos eventos se cruzan utilizo esta condición:

```java
start.isBefore(otherEnd)
        && end.isAfter(otherStart)
```

Esto detecta casos como:

```text
Evento A
10:00 -------- 11:00

Evento B
       10:30 -------- 11:30
```

pero permite:

```text
10:00 -------- 11:00
               11:00 -------- 12:00
```

## Cálculo de disponibilidad

Para encontrar disponibilidad, el algoritmo recibe:

* la jornada que se quiere consultar;
* los eventos existentes;
* la duración mínima requerida.

Por ejemplo, si el horario es:

```text
09:00 --------------------------- 17:00
```

y existe:

```text
10:00 -------- 11:00
```

la respuesta será:

```text
09:00 - 10:00
11:00 - 17:00
```

No genero todas las posibles reuniones de una hora dentro del espacio `11:00 - 17:00`.

Simplemente devuelvo el intervalo libre completo y verifico que pueda contener la duración solicitada.

El algoritmo primero filtra los eventos relevantes, luego los ordena por hora de inicio y finalmente recorre la lista utilizando un cursor.

Por eso su coste dominante es aproximadamente:

```text
O(n log n)
```

principalmente debido al ordenamiento.

## Persistencia

Para la demo utilicé H2.

No convertí directamente `Event` en una entidad JPA. Preferí mantener dos conceptos separados:

```text
Event
    ↕
EventJpaEntity
```

`EventRepository` funciona como contrato entre la aplicación y la persistencia.

Después `EventRepositoryAdapter` implementa ese contrato utilizando Spring Data JPA.

Esto permite que el dominio no tenga anotaciones como `@Entity`, `@Table`, etc.

La base está configurada en modo archivo:

```properties
spring.datasource.url=jdbc:h2:file:./data/calendar
```

Por eso los eventos permanecen guardados incluso después de detener y volver a iniciar la aplicación.

## Manejo de fechas

Dentro del dominio utilizo `Instant`.

La API, en cambio, recibe `OffsetDateTime`.

Por ejemplo:

```text
2026-08-24T09:00:00-05:00
```

Las reglas del calendario se evalúan utilizando:

```text
America/Bogota
```

La idea es separar tres cosas:

```text
Momento absoluto      → Instant
Dato recibido por API → OffsetDateTime
Reglas locales         → America/Bogota
```

## API

### Crear un evento

```http
POST /events
```

```json
{
  "title": "Technical interview",
  "description": "Opcional, máximo 500 caracteres",
  "start": "2026-08-24T09:00:00-05:00",
  "end": "2026-08-24T10:00:00-05:00"
}
```

El título es obligatorio (máximo 100 caracteres). Si falta el título, el inicio o el fin, la API responde `400` con código `INVALID_REQUEST`.

### Consultar eventos

```http
GET /events?from=2026-08-24T09:00:00-05:00&to=2026-08-24T17:00:00-05:00
```

### Actualizar

```http
PUT /events/{id}
```

### Eliminar

```http
DELETE /events/{id}
```

### Consultar disponibilidad

```http
GET /availability?date=2026-08-24&durationMinutes=60
```

Ejemplo:

```json
[
  {
    "start": "2026-08-24T09:00:00-05:00",
    "end": "2026-08-24T10:00:00-05:00"
  },
  {
    "start": "2026-08-24T11:00:00-05:00",
    "end": "2026-08-24T17:00:00-05:00"
  }
]
```

## Errores de negocio

También quise evitar que cualquier error terminara simplemente como un `500`.

Por eso existen excepciones específicas:

```text
EventNotFoundException
EventOverlapException
OutsideSchedulingHoursException
```

Estas se transforman en respuestas HTTP desde un `GlobalExceptionHandler`.

Por ejemplo, un conflicto entre eventos responde:

```text
409 Conflict
```

```json
{
  "code": "EVENT_OVERLAP",
  "message": "Event overlaps with an existing event"
}
```

Un evento inexistente responde `404`, y un horario no permitido responde `400`.

## Pruebas

Las pruebas están hechas con JUnit 5.

Para mantenerlas fáciles de leer utilicé el patrón AAA:

```text
Arrange
Act
Assert
```

Es decir:

```java
// Arrange
// preparar escenario

// Act
// ejecutar comportamiento

// Assert
// comprobar resultado
```

### Pruebas unitarias

Las pruebas unitarias validan componentes como:

```text
Event
TimeSlot
SchedulingPolicy
AvailabilityCalculator
ScheduleEventUseCase
GetEventsUseCase
FindAvailabilityUseCase
UpdateEventUseCase
DeleteEventUseCase
```

Para probar los casos de uso sin depender de H2 existe un `InMemoryEventRepository` dentro de los tests.

Esto funciona como un repositorio falso y permite probar únicamente la lógica.

### Pruebas de integración

También existen pruebas que levantan el contexto completo de Spring utilizando `SpringBootTest` y `MockMvc`.

Estas pruebas recorren prácticamente todo el flujo:

```text
HTTP
 ↓
Controller
 ↓
Use Case
 ↓
Domain
 ↓
Repository
 ↓
JPA
 ↓
H2
```

Entre otras cosas se prueba:

* creación;
* consulta;
* actualización;
* eliminación;
* disponibilidad;
* eventos solapados;
* horarios inválidos;
* sábado;
* domingo.

Para ejecutar todas las pruebas:

```powershell
.\gradlew clean test
```

## Pruebas de rendimiento

Además de comprobar que el algoritmo funciona, quería saber cómo se comportaba al aumentar la cantidad de eventos.

Para esto utilicé JMH sobre `AvailabilityCalculator`.

El benchmark prueba con:

```text
10
100
1.000
10.000 eventos
```

Los resultados obtenidos en mi entorno fueron:

| Eventos |       Promedio |
| ------: | -------------: |
|      10 |    0.237 µs/op |
|     100 |    3.660 µs/op |
|   1.000 |   46.761 µs/op |
|  10.000 | 1116.678 µs/op |

Con 10.000 eventos el cálculo tardó aproximadamente:

```text
1.12 ms por operación
```

Esto no pretende ser un SLA de producción. Es simplemente una medición reproducible que permite observar cómo escala el algoritmo.

El benchmark utiliza warm-up porque en Java medir directamente con `System.nanoTime()` puede producir resultados engañosos debido al JIT, garbage collector y otras optimizaciones de la JVM.

Para ejecutarlo:

```powershell
.\gradlew jmh
```

## Interfaz web

La interfaz está hecha con HTML, CSS y JavaScript sin frameworks (`src/main/resources/static/index.html`) y la sirve el mismo Spring Boot. Consume la API real; no tiene datos simulados.

No utilicé React, Angular o algún framework similar porque una sola página es suficiente y no agrega dependencias ni un segundo proceso de build.

Permite:

* ver el calendario por mes, semana o día y navegar entre periodos;
* crear un evento haciendo clic en un día u hora libre, o con **+ Nuevo evento**;
* ver el detalle de un evento, editarlo o eliminarlo (con confirmación);
* buscar por título o descripción dentro del periodo visible;
* buscar espacios disponibles y crear un evento directamente en uno de ellos;
* ver mensajes claros en español para errores de validación, conflictos y horarios no permitidos.

Está disponible automáticamente al ejecutar la aplicación en:

```text
http://localhost:8080
```

## Ejecutar el proyecto

Levantar la aplicación:

```powershell
.\gradlew bootRun
```

Ejecutar pruebas (genera también el reporte de cobertura JaCoCo en `build/reports/jacoco/test/html/index.html`):

```powershell
.\gradlew clean test
```

> `gradlew` usa el JDK de `JAVA_HOME` (debe ser Java 21). Si `java -version` muestra otra versión, no ejecutes el jar con ese `java`.

Ejecutar benchmarks:

```powershell
.\gradlew jmh
```

Compilar:

```powershell
.\gradlew build
```

## Consola H2

Con la aplicación ejecutándose también está disponible:

```text
http://localhost:8080/h2-console
```

Configuración:

```text
JDBC URL: jdbc:h2:file:./data/calendar
User Name: sa
Password: vacío
```

La consola es principalmente útil para revisar el contenido de la base.

Las operaciones normales deberían hacerse utilizando la API para que pasen por las reglas del negocio.

## En resumen

El proyecto es intencionalmente pequeño.

No buscaba construir un reemplazo de Google Calendar, sino resolver correctamente un problema concreto y poder explicar las decisiones tomadas.

Las partes que más me interesaba trabajar fueron:

```text
reglas de negocio claras
+
separación entre dominio e infraestructura
+
pruebas
+
persistencia
+
manejo correcto del tiempo
+
medición de rendimiento
```

La aplicación podría haberse construido únicamente con controllers, services y repositories, pero separar el dominio y los casos de uso permitió que buena parte de la lógica pudiera probarse sin levantar Spring ni una base de datos.

Ese fue el principal criterio utilizado durante el desarrollo.
