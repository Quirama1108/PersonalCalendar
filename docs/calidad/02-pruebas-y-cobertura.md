# 2. Pruebas y cobertura

## 2.1 Framework

- **JUnit 5** (Jupiter) para todas las pruebas.
- **MockMvc** + `@SpringBootTest` para las pruebas de integración HTTP.
- `InMemoryEventRepository` (en `src/test/java/com/calendar/support`) es un repositorio falso: permite probar casos de uso sin Spring ni base de datos.
- Patrón **AAA** (Arrange, Act, Assert) en todas las pruebas, como en el código original.

Las pruebas de integración usan **H2 en memoria** (`src/test/resources/application.properties`). Antes usaban la base real `./data/calendar` y la vaciaban en cada ejecución.

## 2.2 Pruebas implementadas

Total: **58 pruebas, 0 fallos** (antes había 41).

| Clase de prueba | Pruebas | Qué valida |
| --------------- | ------: | ---------- |
| `EventTest` | 8 | Fechas válidas, inicio = fin, fin antes de inicio, solapamiento, eventos consecutivos, **título vacío o nulo**, **recorte de espacios y descripción por defecto**, **longitudes máximas (100 / 500) y sus límites exactos** |
| `TimeSlotTest` | 4 | Construcción, duración, `canFit` |
| `SchedulingPolicyTest` | 9 | Lunes a viernes, antes de 9, después de 17, sábado hasta 16, domingo, cruce de día, **jornada completa 09:00–17:00**, **08:59** |
| `AvailabilityCalculatorTest` | 8 | Ventana libre, huecos entre eventos, **recorte a la ventana**, **eventos fuera de la ventana y desordenados**, **solapados y anidados**, **huecos más cortos que la duración**, **día lleno**, **duración cero** |
| `ScheduleEventUseCaseTest` | 5 | Creación válida, fuera de horario, conflicto, **guarda la descripción**, **no guarda nada si el título está vacío** |
| `UpdateEventUseCaseTest` | 6 | Edición, inexistente, fuera de horario, conflicto, no choca consigo mismo, **actualiza descripción y un cambio rechazado no modifica lo guardado** |
| `GetEventsUseCaseTest` | 2 | Rango ordenado, rango inválido |
| `DeleteEventUseCaseTest` | 2 | Eliminación, inexistente |
| `FindAvailabilityUseCaseTest` | 3 | Día hábil, domingo, sábado |
| `CalendarIntegrationTest` | 11 | Flujo HTTP completo + **descripción persistida**, **400 sin inicio o con título vacío**, **404 al eliminar inexistente** |

En negrita, las pruebas agregadas en este trabajo. Cada una prueba una regla de negocio o un caso límite, no solo líneas de código.

Además se verificó la interfaz gráfica en Chrome *headless* (21 comprobaciones: vistas, validación, crear, conflicto, domingo, búsqueda, detalle, editar, disponibilidad, eliminar con y sin confirmación, navegación de enero 31 a febrero, móvil sin desplazamiento horizontal, ningún error de JavaScript). Ese script se ejecutó durante el desarrollo y no forma parte del repositorio.

## 2.3 Comandos

```powershell
# Pruebas + reporte de cobertura (JaCoCo se ejecuta automáticamente después de test)
.\gradlew.bat clean test

# Todo: compilar, pruebas, cobertura y medición de complejidad (PMD)
.\gradlew.bat build

# Solo una clase de pruebas
.\gradlew.bat test --tests "*AvailabilityCalculatorTest"
```

`gradlew` usa el JDK de `JAVA_HOME`, que debe ser Java 21.

## 2.4 Configuración de JaCoCo

En `build.gradle`:

```groovy
plugins {
    id 'jacoco'
}

test {
    useJUnitPlatform()
    finalizedBy jacocoTestReport   // el reporte se genera siempre después de las pruebas
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true        // lo lee SonarCloud
        html.required = true       // para consultar localmente
    }
}
```

## 2.5 Rutas de los reportes

El proyecto usa Gradle, no Maven: la ruta equivalente a `target/site/jacoco/index.html` es:

| Reporte | Ruta |
| ------- | ---- |
| Cobertura HTML | `build/reports/jacoco/test/html/index.html` |
| Cobertura XML (SonarCloud) | `build/reports/jacoco/test/jacocoTestReport.xml` |
| Resultados de pruebas | `build/reports/tests/test/index.html` |
| Complejidad (PMD) | `build/reports/pmd/main.html` |

Abrir desde PowerShell:

```powershell
start build\reports\jacoco\test\html\index.html
```

## 2.6 Resultados reales de cobertura (JaCoCo)

| Contador      | Antes                | Después              |
| ------------- | -------------------- | -------------------- |
| Instrucciones | 925/958 = 96,6 %     | 985/990 = **99,5 %** |
| Ramas         | 55/68 = 80,9 %       | 54/56 = **96,4 %**   |
| Líneas        | 274/285 = 96,1 %     | 277/279 = **99,3 %** |
| Métodos       | 72/75 = 96,0 %       | 86/87 = **98,9 %**   |
| Clases        | 23/23 = 100 %        | 24/24 = **100 %**    |

Por paquete (después):

| Paquete | Líneas | Ramas |
| ------- | -----: | ----: |
| `domain` | 89/89 | 40/42 |
| `application/usecase` | 64/64 | 12/12 |
| `infrastructure/web` | 55/55 | 2/2 |
| `infrastructure/persistence` | 41/41 | — |
| `infrastructure/config` | 8/8 | — |
| `infrastructure/web/error` | 10/10 | — |
| `com.calendar` (`CalendarApplication`) | 1/3 | — |

La subida de ramas (80,9 % → 96,4 %) se debe sobre todo a las pruebas nuevas de `AvailabilityCalculator` y a que el refactor eliminó ramas que no se podían ejecutar (la condición redundante `eventEnd.isAfter(cursor)` siempre era verdadera). El número total de ramas bajó de 68 a 56, así que parte de la mejora viene de tener menos ramas, no solo de más pruebas.

**Aclaración:** JaCoCo mide qué partes del código **ejecutaron** las pruebas. No mide calidad del código ni complejidad cognitiva. Su HTML muestra una columna *Cxty* (complejidad ciclomática calculada sobre el bytecode) solo para relacionar caminos con cobertura; las métricas de complejidad de este informe vienen de PMD y SonarCloud.

## 2.7 Áreas sin cobertura

| Ubicación | Qué falta | Por qué / riesgo |
| --------- | --------- | ---------------- |
| `CalendarApplication.main` | 2 líneas | Solo arranca Spring. `@SpringBootTest` levanta el contexto sin llamar a `main`. Excluido de la cobertura en SonarCloud. Riesgo nulo. |
| `Event.overlaps` | 1 rama | El caso en que el primer operando de `&&` es falso (el otro evento termina antes de que este empiece) no se prueba directamente. Riesgo bajo: la expresión se prueba en los demás sentidos. |
| `AvailabilityCalculator.findAvailableSlots` | 1 rama | Duración **negativa**: se prueba duración cero pero no negativa. Riesgo bajo: misma excepción. |

Ninguna de estas brechas está en una regla de negocio sin probar.
