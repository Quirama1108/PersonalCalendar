# 3. Antes y después

## 3.1 Problemas iniciales

| Área | Problema |
| ---- | -------- |
| Datos | Ejecutar `gradlew test` borraba los eventos guardados: las pruebas de integración usaban la base real y llamaban a `deleteAll()`. |
| Seguridad | La interfaz construía HTML con `innerHTML` y el título del evento (XSS). |
| Validación | Título vacío permitido; falta de `start`/`end` producía `500` en lugar de `400`. |
| Interfaz | Sin vista de calendario (solo formularios y listas), sin edición, sin detalle, eliminación sin confirmación, fecha fija `2026-08-24`, errores del servidor en inglés y sin manejo de fallos de conexión al consultar. |
| Funcionalidad | Los eventos no tenían descripción. No había búsqueda. |
| Duplicación | Validación repetida en crear/editar; zona horaria declarada 3 veces; hora de cierre calculada 2 veces. |
| Complejidad | `findAvailableSlots`: CC 14, Cog 22. |
| Herramientas | Sin JaCoCo, sin SonarCloud, sin CI. |

## 3.2 Mejoras funcionales

- **Descripción** opcional en los eventos (máx. 500 caracteres), en dominio, base de datos, API e interfaz. La columna se agrega sola sobre la base existente (`ddl-auto=update`); se verificó con una **copia** de la base real: los eventos existentes (se revisaron los 16 de agosto de 2026) se leen con descripción vacía.
- **Validación** en el dominio: título obligatorio (sin espacios sobrantes, máx. 100 caracteres). En la API: `start`/`end` obligatorios → `400 INVALID_REQUEST`.
- **Interfaz gráfica nueva** (ver 3.3).
- **Búsqueda** por título o descripción, sin distinguir mayúsculas ni tildes ("gestión" encuentra "Gestion").
- Se conservaron todas las funcionalidades anteriores: crear, consultar por rango, editar, eliminar, disponibilidad, detección de conflictos y reglas de horario. La API anterior sigue siendo compatible (`description` es opcional).

## 3.3 Interfaz gráfica

**Decisión tecnológica.** El proyecto es una API REST de Spring Boot que ya servía una página HTML. Se evolucionó esa página (HTML, CSS y JavaScript sin frameworks) en lugar de agregar Swing o JavaFX, porque:

- una interfaz de escritorio tendría que volver a conectarse por HTTP o duplicar la configuración de Spring;
- no agrega dependencias ni otro proceso de build;
- se abre con `bootRun` en `http://localhost:8080`.

**Contenido.**

- Encabezado: navegación (‹ Hoy ›), periodo actual, selector **Mes / Semana / Día**, buscador y botón **+ Nuevo evento**.
- **Mes**: cuadrícula de 6 semanas; domingos sombreados; hasta 3 eventos por día y "+N más" (lleva a la vista de día). Clic en un día crea un evento ese día.
- **Semana / Día**: rejilla por horas con las horas no permitidas sombreadas; clic en una hora libre crea un evento a esa hora. La rejilla se amplía si hay eventos guardados fuera de 08:00–18:00 (la base real los tiene).
- **Detalle / edición**: clic en un evento abre un diálogo con todos sus datos para editar o eliminar (pide confirmación).
- Panel lateral: lista de eventos del periodo (o resultados de búsqueda), **buscador de disponibilidad** (clic en un espacio libre crea un evento ahí) y las reglas de horario.
- Mensajes: validación en el formulario antes de enviar; errores del servidor traducidos al español (conflicto, fuera de horario, no encontrado); avisos de éxito.
- Accesible: etiquetas en todos los campos, foco visible, `aria-live` para mensajes, funciona con teclado.
- *Responsive*: en móvil el panel lateral pasa debajo y no hay desplazamiento horizontal (verificado a 390 px).
- Sin datos simulados: todo viene de la API real.

## 3.4 Cambios de arquitectura

Se mantuvo la arquitectura hexagonal. Cambios puntuales:

| Archivo | Cambio |
| ------- | ------ |
| `application/usecase/SchedulingGuard.java` (nuevo) | Validación compartida de horario y conflictos para crear y editar. |
| `domain/Event.java` | Campo `description`; validación de título y longitudes. El constructor anterior de 4 parámetros se conserva. |
| `domain/SchedulingPolicy.java` | `ZONE` pública y única; `isAllowed` reutiliza `availabilityFor`. |
| `infrastructure/persistence/EventJpaEntity.java` | Columna `description` (opcional). |
| `infrastructure/web/EventController.java` | Valida `start`/`end`; mapea `description`. |
| `src/test/resources/application.properties` (nuevo) | H2 en memoria para pruebas. |
| `build.gradle` | JaCoCo, PMD (medición) y SonarCloud. |
| `config/pmd/complexity.xml` (nuevo) | Reglas de medición de complejidad. |
| `.github/workflows/build.yml` (nuevo) | CI: build, pruebas, cobertura y SonarCloud (si existe el secreto). |

## 3.5 Refactorizaciones

1. `AvailabilityCalculator.findAvailableSlots` → bucle de 2 líneas con `eventsWithin`, `addIfFits` y `latest` (detalle en el documento 1, sección 1.6).
2. `SchedulingPolicy.isAllowed` → reutiliza la jornada de `availabilityFor` en vez de recalcular hora de apertura, cierre y domingo. Un evento que cruza al día siguiente termina después del cierre, así que la comprobación "mismo día" ya no hace falta.
3. `ScheduleEventUseCase` / `UpdateEventUseCase` → delegan en `SchedulingGuard`.
4. `Event` → la validación nueva se dividió en `requireNotBlank` y `withMaxLength`: la primera versión había subido el constructor a CC 10 / Cog 6 y se corrigió a 4 / 2.

Cada refactor se validó con la suite completa (58/58). Para el punto 1 se escribieron primero pruebas de caracterización que pasaron con el código original.

## 3.6 Comparación de métricas

| Métrica | Antes | Después | Fuente |
| ------- | ----: | ------: | ------ |
| Pruebas | 41 | 58 | JUnit |
| Cobertura de líneas | 96,1 % | 99,3 % | JaCoCo |
| Cobertura de ramas | 80,9 % | 96,4 % | JaCoCo |
| Máx. complejidad ciclomática por método | 14 | 5 | PMD 7.13 |
| Máx. complejidad cognitiva por método | 22 | 3 | PMD 7.13 |
| Métodos con Cog > 15 (umbral de `S3776`) | 1 | 0 | PMD 7.13 |
| Bloques de validación duplicados (crear/editar) | 2 | 1 | Revisión de código |
| Declaraciones de la zona horaria | 3 | 1 | Revisión de código |

No hay medición de SonarCloud antes o después: el análisis no se pudo ejecutar sin token.

## 3.7 Problemas pendientes

| Pendiente | Detalle | Recomendación |
| --------- | ------- | ------------- |
| Análisis SonarCloud sin ejecutar | Falta crear el proyecto y el token. Estado del Quality Gate desconocido. | Seguir la guía (documento 4, anexo C). |
| `data/calendar.mv.db` está versionado | El repositorio es público y el archivo contiene los eventos personales. | Decidir si se saca del control de versiones (`git rm --cached data/calendar.mv.db` y agregar `data/` a `.gitignore`). No se hizo para no tocar datos sin autorización. |
| Eventos antiguos fuera de horario | La base real tiene eventos de 06:00–08:00 y 18:00–22:00 que hoy la regla de horario no permitiría. Se muestran bien, pero para editarlos hay que moverlos a un horario permitido. | Decidir si se ajustan las reglas o los datos. |
| `gradlew` sin permiso de ejecución en Git | En Linux `./gradlew` falla. El workflow hace `chmod +x`. | `git update-index --chmod=+x gradlew` y commit. |
| Búsqueda limitada al periodo visible | Se filtra en el navegador sobre los eventos cargados. | Si se necesita búsqueda global, agregar un parámetro `q` a `GET /events`. |
| Desfase fijo `-05:00` en la interfaz | Correcto para Bogotá (no tiene horario de verano). | Si la zona cambia, leerla del servidor. |
| Mensajes de error del backend en inglés | La interfaz traduce los códigos de negocio; los mensajes `INVALID_REQUEST` llegan en inglés. | Traducirlos si la API se publica en español. |
| 3 elementos sin cobertura | Ver documento 2, sección 2.7. | Opcional: prueba con duración negativa y con eventos disjuntos en `overlaps`. |
