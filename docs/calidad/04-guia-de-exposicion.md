# 4. Guía de exposición (guion de diapositivas)

> **Instrucciones para quien genere la presentación (persona o IA como ChatGPT):**
>
> - Crear **una diapositiva por cada sección "Diapositiva N"**, en el mismo orden.
> - Usar el **Título** tal cual, los puntos de **Contenido** como viñetas (máximo 5 por diapositiva) y las **Notas del orador** como notas, no en la diapositiva.
> - Donde diga **Visual**, poner esa tabla, código o captura. El código debe ir en fuente monoespaciada y sin cambios.
> - **No inventar cifras.** Todos los números de este documento son mediciones reales. Si un dato aparece como *pendiente*, mostrarlo como pendiente.
> - Idioma: español. Tono: académico y claro. Duración total: 12–15 minutos.

**Datos del proyecto**

| | |
| - | - |
| Proyecto | Personal Calendar: API REST de calendario personal con interfaz web |
| Tecnologías | Java 21, Spring Boot 4.1, JPA, H2, Gradle, JUnit 5, JaCoCo, PMD 7.13, SonarCloud, GitHub Actions |
| Arquitectura | Hexagonal: `domain` (reglas), `application` (casos de uso y puertos), `infrastructure` (web, persistencia, configuración) |
| Repositorio | https://github.com/Quirama1108/PersonalCalendar |
| Reglas de negocio | Lunes a viernes de 09:00 a 17:00, sábado de 09:00 a 16:00, domingo cerrado (zona America/Bogota). Los eventos no pueden cruzarse. |

---

## Diapositiva 1: Calidad de software en Personal Calendar

**Contenido**

- Análisis estático, pruebas y cobertura aplicados a un proyecto real
- Herramientas: SonarCloud, JaCoCo, PMD y GitHub Actions
- Resultado: menos complejidad, más pruebas y defectos reales corregidos

**Notas del orador:** Presentar el proyecto en una frase: un calendario personal con API REST y una interfaz web, con arquitectura hexagonal. El objetivo del trabajo fue medir la calidad, mejorarla y demostrar la mejora con datos.

---

## Diapositiva 2: ¿Qué es el análisis estático?

**Contenido**

- Revisar el código **sin ejecutarlo**
- Encuentra errores probables, vulnerabilidades, duplicación y complejidad
- Actúa antes de que el problema llegue a producción

**Visual:** el defecto encontrado en el proyecto.

```javascript
// Antes (vulnerable a XSS): un título con HTML se ejecutaba en la página
element.innerHTML = `... ${event.title} ...`;
// Después
element.textContent = event.title;
```

**Notas del orador:** Un análisis de seguridad marca `innerHTML` con datos del usuario. Si alguien guarda un evento titulado `<img src=x onerror=alert(1)>`, ese código se ejecuta. Con `textContent`, el navegador lo muestra como texto.

---

## Diapositiva 3: ¿Qué evalúa SonarCloud?

**Visual:**

| Dimensión | Pregunta | Ejemplo en el proyecto |
| --------- | -------- | ---------------------- |
| Fiabilidad (*bugs*) | ¿Falla en ejecución? | `request.start().toInstant()` sin validar → `NullPointerException` → error `500` |
| Seguridad | ¿Se puede atacar? | XSS en la interfaz original |
| Mantenibilidad (*code smells*) | ¿Es fácil de cambiar? | `findAvailableSlots` con complejidad cognitiva 22 (límite 15) |
| Duplicación | ¿Hay código copiado? | Validación de horario y conflictos repetida al crear y al editar |
| Cobertura | ¿Las pruebas ejecutan el código? | La lee del XML de JaCoCo |

**Notas del orador:** Cada fila es un problema real que se encontró y corrigió en este proyecto.

---

## Diapositiva 4: JaCoCo y la cobertura

**Contenido**

- JaCoCo instrumenta el *bytecode* mientras corren las pruebas
- **Cobertura de líneas:** ¿se ejecutó la línea?
- **Cobertura de ramas:** en un `if`, ¿se probaron el caso verdadero **y** el falso?
- **Cobertura alta no significa pruebas buenas:** una prueba sin `assert` sube la cobertura y no verifica nada
- JaCoCo **no** mide complejidad ni calidad; eso lo hacen SonarCloud y PMD

**Notas del orador:** Las pruebas nuevas comprueban reglas concretas, por ejemplo "un cambio rechazado no modifica lo guardado", no solo que el código se ejecute.

---

## Diapositiva 5: Complejidad ciclomática vs. cognitiva

**Visual:**

| | Ciclomática | Cognitiva |
| - | ----------- | --------- |
| Pregunta | ¿Cuántos caminos hay? | ¿Cuánto cuesta entenderlo? |
| Cuenta | Cada decisión: `if`, bucle, `case`, `&&`, `\|\|`, `?:` | Cada ruptura del flujo, **más un extra por anidamiento** |
| Sirve para | Saber cuántas pruebas mínimas se necesitan | Saber qué refactorizar |
| En SonarCloud | Métrica (*Measures → Complexity*); la regla `S1541` no está activa | Regla `java:S3776`, activa, umbral 15 |

**Notas del orador:** Un `if` suelto suma 1 a las dos. El mismo `if` dentro de un `for` sigue sumando 1 a la ciclomática, pero suma 2 a la cognitiva: 1 por el `if` y 1 por estar anidado.

---

## Diapositiva 6: Caso real: `findAvailableSlots` (antes)

**Contenido**

- Busca espacios libres en un día para una duración dada
- Ciclomática **14**, cognitiva **22**: supera el umbral 15 de SonarCloud
- 7 decisiones dentro del `for`, con `continue`, `break` y 3 niveles de anidamiento
- Una condición siempre verdadera (código muerto)
- La lógica "¿cabe el hueco?" escrita dos veces

**Visual:**

```java
for (Event event : sortedEvents) {
    Instant eventStart = event.getStart().isBefore(searchWindow.start())
            ? searchWindow.start() : event.getStart();
    Instant eventEnd = event.getEnd().isAfter(searchWindow.end())
            ? searchWindow.end() : event.getEnd();
    if (!eventEnd.isAfter(cursor)) { continue; }
    if (cursor.isBefore(eventStart)) {
        TimeSlot freeSlot = new TimeSlot(cursor, eventStart);
        if (freeSlot.canFit(requiredDuration)) {
            availableSlots.add(freeSlot);
        }
    }
    if (eventEnd.isAfter(cursor)) { cursor = eventEnd; }   // siempre verdadero
    if (!cursor.isBefore(searchWindow.end())) { break; }
}
if (cursor.isBefore(searchWindow.end())) {                 // lógica repetida
    TimeSlot freeSlot = new TimeSlot(cursor, searchWindow.end());
    if (freeSlot.canFit(requiredDuration)) { availableSlots.add(freeSlot); }
}
```

---

## Diapositiva 7: Caso real: `findAvailableSlots` (después)

**Contenido**

- Ciclomática **5**, cognitiva **3**
- El bucle quedó en 2 líneas; cada idea tiene su propio método
- `addIfFits`: la regla "el hueco cabe" vive en un solo lugar
- `latest`: avanza el cursor sin `if`, `continue` ni `break`
- **Primero se escribieron 6 pruebas de caracterización** que pasaron con el código viejo; después se refactorizó y siguieron pasando

**Visual:**

```java
for (Event event : eventsWithin(searchWindow, events)) {
    addIfFits(availableSlots, cursor, event.getStart(), requiredDuration);
    cursor = latest(cursor, event.getEnd());
}
addIfFits(availableSlots, cursor, searchWindow.end(), requiredDuration);
```

**Notas del orador:** Las pruebas de caracterización fijan el comportamiento actual antes de cambiar el código. Si siguen pasando después del refactor, el comportamiento no cambió. Cubren eventos que empiezan antes o terminan después de la ventana, entrada desordenada, eventos solapados y anidados, huecos demasiado cortos y la ventana completamente ocupada.

---

## Diapositiva 8: Eliminar duplicación: `SchedulingGuard`

**Contenido**

- Crear y editar un evento repetían la misma validación de horario y conflictos
- Ahora los dos casos de uso llaman a una sola clase
- Al editar, se excluye el propio evento para que no choque consigo mismo
- La zona horaria estaba declarada 3 veces; ahora es una sola (`SchedulingPolicy.ZONE`)

**Visual:**

```java
static void ensureCanBeScheduled(Event event, SchedulingPolicy policy, EventRepository repo) {
    if (!policy.isAllowed(event.getStart(), event.getEnd())) {
        throw new OutsideSchedulingHoursException();
    }
    // Se excluye el propio evento para que una edición no choque consigo misma.
    boolean hasConflict = repo.findBetween(event.getStart(), event.getEnd()).stream()
            .filter(existing -> !existing.getId().equals(event.getId()))
            .anyMatch(existing -> existing.overlaps(event));
    if (hasConflict) {
        throw new EventOverlapException();
    }
}
```

---

## Diapositiva 9: Quality Profile vs. Quality Gate

**Visual:**

| | Quality Profile | Quality Gate |
| - | --------------- | ------------ |
| Es… | El conjunto de **reglas** | El conjunto de **condiciones** de aprobación |
| Responde | ¿Qué problemas busco? | ¿Puedo publicar este código? |
| Resultado | *Issues* | **Passed / Failed** |
| En este proyecto | **Sonar way comprehensive** (Java, 659 reglas) | **Sonar way** |

**Contenido**

- Condiciones de "Sonar way" sobre el **código nuevo**: rating A en seguridad, fiabilidad y mantenibilidad; cobertura ≥ 80 %; duplicación ≤ 3 %; 100 % de *security hotspots* revisados
- Analogía: el perfil es el **reglamento** del árbitro; el gate es la **nota mínima** para aprobar

**Notas del orador:** El gate mira solo el código nuevo ("Clean as You Code") para que un equipo pueda mejorar sin arreglar todo el código heredado de una vez.

---

## Diapositiva 10: Defectos reales corregidos

**Contenido**

- **Las pruebas borraban los datos del usuario:** usaban la base real y llamaban a `deleteAll()`. Ahora usan H2 en memoria (`src/test/resources/application.properties`)
- **XSS** en la interfaz → `textContent`
- **Error 500 por petición incompleta** → si falta `start` o `end`, la API responde `400 INVALID_REQUEST`
- **Título vacío permitido** → el dominio exige título (máx. 100 caracteres)

**Notas del orador:** El primer defecto es el mejor ejemplo de que la cobertura no lo es todo: con 96 % de cobertura y todas las pruebas en verde, ejecutar `gradlew test` borraba los eventos guardados. El defecto estaba en la configuración, que la cobertura no mide.

---

## Diapositiva 11: Mejoras funcionales e interfaz gráfica

**Contenido**

- Vistas de mes, semana y día; crear, ver detalle, editar y eliminar con confirmación
- Descripción opcional en los eventos (máx. 500 caracteres)
- Búsqueda sin distinguir mayúsculas ni tildes
- Buscador de disponibilidad: clic en un espacio libre abre el formulario ya lleno
- Accesible con teclado, mensajes en español y adaptable a móvil

**Visual:** captura de la vista de semana con un evento y las horas no permitidas sombreadas.

**Notas del orador:** Se evolucionó la página HTML que ya servía Spring, en lugar de agregar Swing o JavaFX: no suma dependencias ni otro proceso de *build*, y se abre con `gradlew bootRun` en `http://localhost:8080`.

---

## Diapositiva 12: Resultados medidos

**Visual:**

| Métrica | Antes | Después | Fuente |
| ------- | ----: | ------: | ------ |
| Pruebas | 41 | 58 (0 fallos) | JUnit |
| Cobertura de líneas | 96,1 % | 99,3 % | JaCoCo |
| Cobertura de ramas | 80,9 % | 96,4 % | JaCoCo |
| Máx. complejidad ciclomática por método | 14 | 5 | PMD 7.13 |
| Máx. complejidad cognitiva por método | 22 | 3 | PMD 7.13 |
| Métodos con cognitiva > 15 | 1 | 0 | PMD 7.13 |
| Bloques de validación duplicados | 2 | 1 | Revisión de código |
| Declaraciones de la zona horaria | 3 | 1 | Revisión de código |

**Notas del orador:** Parte de la subida de ramas viene de que el refactor eliminó ramas imposibles (de 68 a 56 ramas), no solo de las pruebas nuevas.

---

## Diapositiva 13: Resultados en SonarCloud

**Estado:** *pendiente*. Completar esta tabla con los valores reales del panel de SonarCloud después de ejecutar el análisis. Si siguen vacíos, mostrar la diapositiva como "análisis configurado, pendiente de ejecución".

| Indicador | Valor |
| --------- | ----- |
| Quality Gate | *pendiente* |
| Bugs / Vulnerabilidades / Code smells | *pendiente* |
| Cobertura (debe coincidir con JaCoCo, ≈ 99 %) | *pendiente* |
| Duplicación | *pendiente* |
| *Issues* de la regla `java:S3776` | *pendiente* |

**Notas del orador:** Mostrar el panel en vivo: *Overview* (estado del gate), *Issues* filtrado por `java:S3776`, *Measures → Coverage*, *Measures → Complexity* y la diferencia entre las pestañas *New Code* y *Overall Code*.

---

## Diapositiva 14: Integración continua

**Contenido**

- GitHub Actions (`.github/workflows/build.yml`) se ejecuta en cada *push* y *pull request* a `main`
- Compila, ejecuta las 58 pruebas y genera los reportes de JaCoCo y PMD
- Si existe el secreto `SONAR_TOKEN`, envía el análisis a SonarCloud y espera el Quality Gate
- Los reportes quedan descargables como artefacto del *workflow*

---

## Diapositiva 15: Pendientes y conclusiones

**Contenido**

- Pendiente: eventos antiguos guardados fuera del horario permitido
- Pendiente: la búsqueda solo mira el periodo visible
- Conclusión: medir primero, refactorizar con pruebas que protejan el comportamiento
- Conclusión: la cobertura es necesaria pero no suficiente

**Notas del orador:** Cerrar con la idea central: las herramientas encuentran problemas, pero las pruebas bien diseñadas son las que permiten corregirlos sin miedo.

---

# Anexo A: Guion de la demo en vivo (≈ 4 min, no es diapositiva)

```powershell
.\gradlew.bat bootRun
```

Abrir `http://localhost:8080`:

1. **Mes:** navegar con ‹ › hasta un mes con eventos. Señalar los domingos sombreados.
2. **Crear:** clic en un día hábil → título "Exposición Calidad", descripción, 10:00–11:00 → Guardar → aviso "Evento creado".
3. **Validación:** crear otro con título vacío → mensaje en el formulario sin llamar al servidor.
4. **Conflicto:** crear otro el mismo día de 10:30 a 11:30 → "Ese horario se cruza con otro evento." (regla del servidor).
5. **Domingo:** intentar en domingo desde **+ Nuevo evento** → "Fuera del horario permitido".
6. **Semana:** cambiar a Semana y mostrar las horas no permitidas sombreadas.
7. **Editar:** clic en el evento → cambiar a 14:00–15:00 → Guardar.
8. **Disponibilidad:** elegir el día y 60 min → Buscar → clic en un espacio → se abre el formulario ya lleno.
9. **Búsqueda:** escribir "exposicion" (sin tilde) → lo encuentra.
10. **Eliminar:** abrir el evento → Eliminar → Cancelar (sigue ahí) → Eliminar → Aceptar.
11. Cerrar la aplicación, volver a abrirla y mostrar que los eventos siguen ahí (persistencia en H2).

# Anexo B: Mostrar reportes (≈ 2 min)

```powershell
.\gradlew.bat clean test
start build\reports\tests\test\index.html          # 58 pruebas, 0 fallos
start build\reports\jacoco\test\html\index.html    # cobertura
.\gradlew.bat pmdMain
start build\reports\pmd\main.html                  # complejidad por método
```

En JaCoCo, entrar a `com.calendar.domain` → `AvailabilityCalculator`: el verde es código cubierto y el amarillo una rama cubierta a medias (la duración negativa no se prueba).

# Anexo C: Configurar y ejecutar SonarCloud

**Una sola vez (la hace el dueño del repositorio):**

1. Entrar a https://sonarcloud.io con GitHub → **+ → Analyze new project** → organización `quirama1108` → `PersonalCalendar` → **Set up**.
2. Verificar que la clave del proyecto sea `Quirama1108_PersonalCalendar` (*Administration → General → Project key*). Si es otra, usarla en la variable `SONAR_PROJECT_KEY`.
3. *Administration → Analysis Method* → **desactivar Automatic Analysis**. Si queda activo, el análisis desde Gradle o desde CI falla y no incluye la cobertura de JaCoCo.
4. *My Account → Security* → generar un token.
5. GitHub → repositorio → *Settings → Secrets and variables → Actions* → **New repository secret**: `SONAR_TOKEN` = el token.

**Ejecutar desde el computador:**

```powershell
$env:SONAR_TOKEN = "<token>"      # solo en la sesión actual; nunca en un archivo del repositorio
.\gradlew.bat build sonar
```

O hacer *push* a `main`: el *workflow* ejecuta el análisis si existe el secreto.

# Anexo D: Preguntas probables

- **¿Por qué no Swing o JavaFX?** La aplicación ya es una API web y ya servía una página HTML. Una interfaz de escritorio tendría que volver a conectarse por HTTP o duplicar la configuración de Spring.
- **¿Por qué PMD si ya está SonarCloud?** Para medir la complejidad antes y después en local, sin depender de un token. SonarCloud es el análisis oficial.
- **¿99 % de cobertura significa que no hay errores?** No. La cobertura indica qué se ejecutó, no si se verificó bien. Con 96 % de cobertura y todas las pruebas en verde, las pruebas borraban la base de datos real.
- **¿Por qué el Quality Gate mira solo el código nuevo?** Para mejorar de forma gradual sin tener que arreglar todo el código heredado de una vez (*Clean as You Code*).
- **¿Cómo saben que el refactor no cambió el comportamiento?** Por las pruebas de caracterización: se escribieron antes del cambio, pasaron con el código original y siguen pasando con el nuevo.
- **¿Qué falta por cubrir?** Dos ramas de bajo riesgo (duración negativa y un caso de `overlaps`) y el método `main`, que solo arranca Spring.
