# 1. Análisis de calidad — Calendario Personal

## 1.1 Descripción del proyecto

Aplicación de calendario personal que permite crear, consultar, editar y eliminar eventos, detectar conflictos de horario y encontrar espacios disponibles respetando un horario laboral:

| Día             | Horario permitido |
| --------------- | ----------------- |
| Lunes a viernes | 09:00 – 17:00     |
| Sábado          | 09:00 – 16:00     |
| Domingo         | Cerrado           |

Los eventos no pueden cruzarse; dos eventos consecutivos (`10:00–11:00` y `11:00–12:00`) sí son válidos, porque los intervalos se tratan como `[inicio, fin)`.

## 1.2 Arquitectura y tecnologías

| Elemento          | Tecnología                                   |
| ----------------- | -------------------------------------------- |
| Lenguaje          | Java 21                                      |
| Build             | Gradle 8.14 (wrapper incluido)               |
| Framework         | Spring Boot 4.1 (Web, Data JPA)              |
| Base de datos     | H2 en archivo (`./data/calendar`)            |
| Pruebas           | JUnit 5, MockMvc (`spring-boot-starter-test`) |
| Cobertura         | JaCoCo (plugin `jacoco` de Gradle)           |
| Complejidad local | PMD 7.13 (plugin `pmd` de Gradle)            |
| Análisis estático | SonarCloud (plugin `org.sonarqube` 6.2)      |
| Rendimiento       | JMH                                          |
| Interfaz gráfica  | HTML + CSS + JavaScript, servida por Spring  |

Arquitectura hexagonal (puertos y adaptadores):

```text
Interfaz web (static/index.html)
        ↓ HTTP/JSON
Controllers (infrastructure/web)
        ↓
Casos de uso (application/usecase)  ──►  SchedulingGuard (validación compartida)
        ↓
Dominio (domain): Event, TimeSlot, SchedulingPolicy, AvailabilityCalculator
        ↓
Puerto EventRepository  ◄──  Adaptador JPA (infrastructure/persistence) → H2
```

El dominio no depende de Spring ni de la base de datos, por eso casi toda la lógica se prueba con JUnit puro y un repositorio en memoria (`InMemoryEventRepository`).

## 1.3 Análisis estático

El análisis estático revisa el código **sin ejecutarlo** para encontrar errores probables (bugs), vulnerabilidades, *code smells*, duplicación y complejidad.

En este proyecto se usan dos herramientas con propósitos distintos:

- **SonarCloud**: análisis oficial del curso. Calcula fiabilidad, seguridad, mantenibilidad, duplicación, cobertura (leyendo el XML de JaCoCo) y aplica el Quality Gate.
- **PMD** (solo local): se configuró únicamente para **medir** la complejidad ciclomática y cognitiva de cada método antes y después de refactorizar, porque el análisis de SonarCloud requiere un token que no estaba disponible durante el trabajo. Su configuración está en `config/pmd/complexity.xml`; los umbrales están en el mínimo para que liste prácticamente todos los métodos. Por eso el build muestra "N PMD rule violations": es la lista de mediciones, no una lista de errores, y no detiene el build.

Configuración de SonarCloud (`build.gradle`, bloque `sonar`):

| Propiedad                              | Valor                                                     |
| -------------------------------------- | --------------------------------------------------------- |
| `sonar.host.url`                       | `https://sonarcloud.io`                                   |
| `sonar.organization`                   | `quirama1108` (verificada en la API pública de SonarCloud) |
| `sonar.projectKey`                     | `Quirama1108_PersonalCalendar` (se puede cambiar con `SONAR_PROJECT_KEY`) |
| `sonar.coverage.jacoco.xmlReportPaths` | `build/reports/jacoco/test/jacocoTestReport.xml`          |
| `sonar.exclusions`                     | `src/jmh/**` (benchmark, no es código de producción)      |
| `sonar.coverage.exclusions`            | `**/CalendarApplication.java` (solo arranca Spring)       |
| Token                                  | variable de entorno `SONAR_TOKEN` (nunca en el repositorio) |

Fuentes y pruebas no se declaran a mano: el plugin de Gradle las toma de los *source sets* (`src/main/java`, `src/test/java`).

**Estado verificado:** al ejecutar `gradlew sonar` sin token, el análisis resuelve la organización y la clave del proyecto y se detiene con `Not authorized. Please check the 'sonar.token' property or the 'SONAR_TOKEN' environment variable`. La configuración es válida; falta crear el proyecto en SonarCloud y el token (ver la guía de exposición, anexo C).

## 1.4 Complejidad ciclomática

Cuenta los **caminos independientes** de un método: empieza en 1 y suma 1 por cada `if`, `case`, bucle, `catch`, operador `?:` y operador lógico `&&`/`||`. Indica cuántas pruebas se necesitan, como mínimo, para recorrer todos los caminos.

## 1.5 Complejidad cognitiva

Mide **qué tan difícil es entender** un método. Suma por cada estructura que rompe el flujo lineal (`if`, `for`, `continue`, `break`, `?:`, secuencias de `&&`/`||`) y **penaliza el anidamiento**: un `if` dentro de un `for` cuesta más que el mismo `if` al nivel superior. SonarCloud la evalúa con la regla `java:S3776`.

## 1.6 Mediciones reales (PMD 7.13)

Valores obtenidos con `gradlew pmdMain` (reporte en `build/reports/pmd/main.html`). **CC** = ciclomática, **Cog** = cognitiva. Los métodos sin decisiones (CC 1, Cog 0) no aparecen.

| Método                                          | Antes CC | Antes Cog | Después CC | Después Cog |
| ----------------------------------------------- | -------: | --------: | ---------: | ----------: |
| `AvailabilityCalculator.findAvailableSlots`     | **14**   | **22**    | 5          | 3           |
| `SchedulingPolicy.isAllowed`                    | 5        | 4         | 1          | 1           |
| `ScheduleEventUseCase.execute`                  | 5        | 2         | 1          | 0           |
| `UpdateEventUseCase.execute`                    | 5        | 2         | 1          | 0           |
| `SchedulingGuard.ensureCanBeScheduled` (nuevo)  | —        | —         | 5          | 2           |
| `Event` (constructor)                           | 3        | 1         | 4          | 2           |
| `SchedulingPolicy.availabilityFor`              | 3        | 2         | 3          | 2           |
| `GetEventsUseCase.execute`                      | 3        | 1         | 3          | 1           |
| `DeleteEventUseCase.execute`                    | 3        | 1         | 3          | 1           |

Máximos del proyecto: **antes CC 14 / Cog 22**, **después CC 5 / Cog 3**.

Notas:

- SonarCloud calcula sus propios valores; pueden diferir levemente de los de PMD. Los valores de esta tabla son los de PMD, no los de SonarCloud.
- En el perfil por defecto de la organización (`Sonar way comprehensive`), la regla de complejidad cognitiva `java:S3776` está **activa** con umbral por defecto 15. La regla de complejidad ciclomática `java:S1541` **no** está activa: SonarCloud muestra la ciclomática como métrica (*Measures → Complexity*), pero no la reporta como problema.
- Con 22 según PMD, `findAvailableSlots` superaba el umbral 15 de `S3776`.

### Ejemplo para la exposición: `findAvailableSlots`

Versión original (resumida):

```java
for (Event event : sortedEvents) {
    Instant eventStart = event.getStart().isBefore(searchWindow.start())
            ? searchWindow.start() : event.getStart();          // ?: anidado en el for
    Instant eventEnd = event.getEnd().isAfter(searchWindow.end())
            ? searchWindow.end() : event.getEnd();              // ?: anidado en el for
    if (!eventEnd.isAfter(cursor)) { continue; }                // if + continue
    if (cursor.isBefore(eventStart)) {                          // if
        TimeSlot freeSlot = new TimeSlot(cursor, eventStart);
        if (freeSlot.canFit(requiredDuration)) {                // if anidado en if, dentro del for
            availableSlots.add(freeSlot);
        }
    }
    if (eventEnd.isAfter(cursor)) { cursor = eventEnd; }        // siempre verdadero tras el continue
    if (!cursor.isBefore(searchWindow.end())) { break; }        // if + break
}
if (cursor.isBefore(searchWindow.end())) {                      // se repite la lógica del hueco
    TimeSlot freeSlot = new TimeSlot(cursor, searchWindow.end());
    if (freeSlot.canFit(requiredDuration)) { availableSlots.add(freeSlot); }
}
```

Por qué era complejo: siete decisiones dentro de un `for`, con hasta tres niveles de anidamiento, `continue` y `break` que saltan el flujo, una condición redundante y la lógica "¿cabe el hueco?" escrita dos veces.

Versión refactorizada:

```java
for (Event event : eventsWithin(searchWindow, events)) {
    addIfFits(availableSlots, cursor, event.getStart(), requiredDuration);
    cursor = latest(cursor, event.getEnd());
}
addIfFits(availableSlots, cursor, searchWindow.end(), requiredDuration);
```

- El recorte a la ventana ya no es necesario: si un evento empieza antes que la ventana, el cursor (que empieza en el inicio de la ventana) nunca queda antes de él, y `addIfFits` descarta huecos vacíos.
- `continue`, `break` y la condición redundante desaparecen: `latest` mantiene el cursor y `addIfFits` ignora huecos de longitud cero o negativa.
- La regla "el hueco cabe" vive en un solo lugar (`addIfFits`).

Cómo se demostró que no cambió el comportamiento: **antes** de refactorizar se agregaron 6 pruebas de caracterización (eventos que empiezan antes o terminan después de la ventana, eventos fuera de la ventana, entrada desordenada, eventos solapados y anidados, huecos demasiado cortos, ventana completamente ocupada, duración cero). Las 8 pruebas de la clase pasaron con el código original y siguen pasando con el refactorizado.

## 1.7 Quality Gate

El **Quality Gate** es la decisión **aprobado / no aprobado** del análisis: un conjunto de condiciones sobre métricas.

Quality Gate de la organización `quirama1108` (consultado en la API pública de SonarCloud el 2026-10-16): **"Sonar way"**, integrado y por defecto. Sus condiciones se evalúan sobre el **código nuevo**:

| Métrica (código nuevo)               | Falla si…        |
| ------------------------------------ | ---------------- |
| Security rating                      | peor que A       |
| Reliability rating                   | peor que A       |
| Maintainability rating               | peor que A       |
| Coverage                             | menor que 80 %   |
| Duplicated lines density             | mayor que 3 %    |
| Security hotspots reviewed           | menor que 100 %  |

**Código nuevo vs. código existente:** el Quality Gate solo exige estas condiciones a lo que cambió dentro del periodo de código nuevo (por defecto, la versión anterior o los cambios del *pull request*). El código existente se sigue analizando y sus problemas se muestran en *Overall Code*, pero no hacen fallar el gate. Así un equipo puede mejorar gradualmente sin tener que corregir todo el código heredado de una vez.

**Estado real del Quality Gate: no disponible todavía.** El proyecto aún no existe en SonarCloud y no hay token, así que no se ejecutó ningún análisis. Lo que sí se puede afirmar con datos locales: la cobertura de líneas es 99,3 % (JaCoCo), por encima del 80 % exigido.

## 1.8 Quality Profile

El **Quality Profile** es el **conjunto de reglas** que se aplican al analizar un lenguaje. Decide *qué se detecta*; el Quality Gate decide *si el resultado es aceptable*.

Perfiles Java de la organización (API pública, 2026-10-16):

| Perfil                    | Reglas activas | Por defecto |
| ------------------------- | -------------: | :---------: |
| Sonar way comprehensive   | 659            | **Sí**      |
| Sonar agentic AI          | 502            |             |
| Sonar way extended        | 378            |             |
| Sonar way core            | 308            |             |

Reglas relevantes del perfil por defecto para este proyecto:

| Regla        | Qué detecta                                   | Parámetro por defecto |
| ------------ | --------------------------------------------- | --------------------- |
| `java:S3776` | Complejidad cognitiva demasiado alta          | umbral 15             |
| `java:S107`  | Métodos o constructores con demasiados parámetros | máx. 7            |
| `java:S1192` | Literales de texto duplicados                 | 3 repeticiones, longitud ≥ 5 |

Personalización: los perfiles integrados no se editan. Para personalizarlos, un administrador de la organización debe **copiar** el perfil (*Quality Profiles → Copy*), activar o desactivar reglas en la copia y asignarla al proyecto. No se modificó ningún perfil ni umbral.

| Quality Profile                       | Quality Gate                                  |
| ------------------------------------- | --------------------------------------------- |
| ¿Qué reglas se revisan?               | ¿El resultado es aceptable?                   |
| Produce *issues*                      | Produce un estado: Passed / Failed            |
| Por lenguaje                          | Por proyecto, para todos los lenguajes        |
| Ej.: activar `S3776` con umbral 15    | Ej.: cobertura de código nuevo ≥ 80 %         |

## 1.9 Hallazgos y mejoras realizadas

| # | Hallazgo | Tipo | Estado |
| - | -------- | ---- | ------ |
| 1 | Las pruebas de integración usaban la base real `./data/calendar` y ejecutaban `deleteAll()`: **correr las pruebas borraba los eventos del usuario**. | Fiabilidad (pérdida de datos) | Corregido: `src/test/resources/application.properties` usa H2 en memoria |
| 2 | La interfaz insertaba el título con `innerHTML`: un título con HTML se ejecutaba en la página (XSS). | Seguridad | Corregido: toda la interfaz usa `textContent` |
| 3 | Una petición sin `start` o `end` lanzaba `NullPointerException` y respondía `500`. | Fiabilidad | Corregido: `400 INVALID_REQUEST` |
| 4 | Se aceptaban títulos vacíos o solo con espacios. | Validación | Corregido en el dominio (`Event`) |
| 5 | `ScheduleEventUseCase` y `UpdateEventUseCase` repetían la misma validación de horario y conflictos. | Duplicación | Extraída a `SchedulingGuard` |
| 6 | La zona `America/Bogota` estaba declarada en 3 clases; la hora de cierre se calculaba en 2 métodos. | Duplicación | Una sola constante `SchedulingPolicy.ZONE`; `isAllowed` reutiliza `availabilityFor` |
| 7 | `findAvailableSlots` con CC 14 / Cog 22. | Mantenibilidad | Refactorizado a CC 5 / Cog 3 |
| 8 | La interfaz no permitía editar ni ver detalles, eliminaba sin confirmar y usaba una fecha fija (2026-08-24). | Usabilidad | Corregido (ver documento 3) |
