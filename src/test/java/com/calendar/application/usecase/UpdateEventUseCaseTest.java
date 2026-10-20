package com.calendar.application.usecase;

import com.calendar.application.exception.EventNotFoundException;
import com.calendar.application.exception.EventOverlapException;
import com.calendar.application.exception.OutsideSchedulingHoursException;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;
import com.calendar.support.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UpdateEventUseCaseTest {

    @Test
    void shouldUpdateExistingEvent() {

        // Arrange
        InMemoryEventRepository repository =
                new InMemoryEventRepository();

        UUID id = UUID.randomUUID();

        repository.save(new Event(
                id,
                "Old title",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        ));

        UpdateEventUseCase useCase =
                new UpdateEventUseCase(
                        repository,
                        new SchedulingPolicy()
                );

        // Act
        Event result = useCase.execute(
                id,
                "Updated meeting",
                Instant.parse("2026-08-24T16:00:00Z"),
                Instant.parse("2026-08-24T17:00:00Z")
        );

        // Assert
        assertEquals(id, result.getId());
        assertEquals("Updated meeting", result.getTitle());
        assertEquals(
                Instant.parse("2026-08-24T16:00:00Z"),
                result.getStart()
        );
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void shouldRejectUpdateWhenEventDoesNotExist() {

        // Arrange
        InMemoryEventRepository repository =
                new InMemoryEventRepository();

        UpdateEventUseCase useCase =
                new UpdateEventUseCase(
                        repository,
                        new SchedulingPolicy()
                );

        UUID id = UUID.randomUUID();

        Instant start = Instant.parse("2026-08-24T14:00:00Z");
        Instant end = Instant.parse("2026-08-24T15:00:00Z");

        // Act
        EventNotFoundException exception = assertThrows(
                EventNotFoundException.class,
                () -> useCase.execute(id, "Unknown", start, end)
        );

        // Assert
        assertEquals(
                "Event not found",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUpdateOutsideAllowedHours() {

        // Arrange
        InMemoryEventRepository repository =
                new InMemoryEventRepository();

        UUID id = UUID.randomUUID();

        repository.save(new Event(
                id,
                "Meeting",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        ));

        UpdateEventUseCase useCase =
                new UpdateEventUseCase(
                        repository,
                        new SchedulingPolicy()
                );

        Instant start = Instant.parse("2026-08-24T13:00:00Z");
        Instant end = Instant.parse("2026-08-24T14:00:00Z");

        // Act
        OutsideSchedulingHoursException exception = assertThrows(
                OutsideSchedulingHoursException.class,
                () -> useCase.execute(id, "Too early", start, end)
        );

        // Assert
        assertEquals(
                "Event is outside allowed scheduling hours",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUpdateWhenItOverlapsAnotherEvent() {

        // Arrange
        InMemoryEventRepository repository =
                new InMemoryEventRepository();

        UUID eventToUpdateId = UUID.randomUUID();

        repository.save(new Event(
                eventToUpdateId,
                "Event to update",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        ));

        repository.save(new Event(
                UUID.randomUUID(),
                "Existing meeting",
                Instant.parse("2026-08-24T16:00:00Z"),
                Instant.parse("2026-08-24T17:00:00Z")
        ));

        UpdateEventUseCase useCase =
                new UpdateEventUseCase(
                        repository,
                        new SchedulingPolicy()
                );

        Instant start = Instant.parse("2026-08-24T16:30:00Z");
        Instant end = Instant.parse("2026-08-24T17:30:00Z");

        // Act
        EventOverlapException exception = assertThrows(
                EventOverlapException.class,
                () -> useCase.execute(eventToUpdateId, "Updated event", start, end)
        );

        // Assert
        assertEquals(
                "Event overlaps with an existing event",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotConflictWithItself() {

        // Arrange
        InMemoryEventRepository repository =
                new InMemoryEventRepository();

        UUID id = UUID.randomUUID();

        repository.save(new Event(
                id,
                "Meeting",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        ));

        UpdateEventUseCase useCase =
                new UpdateEventUseCase(
                        repository,
                        new SchedulingPolicy()
                );

        // Act
        Event result = useCase.execute(
                id,
                "Renamed meeting",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        );

        // Assert
        assertEquals(id, result.getId());
        assertEquals("Renamed meeting", result.getTitle());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void shouldUpdateDescriptionAndKeepItWhenALaterChangeIsRejected() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();
        UUID id = UUID.randomUUID();
        Event original = new Event(
                id,
                "Planning",
                "Original",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        );
        repository.save(original);

        UpdateEventUseCase useCase = new UpdateEventUseCase(
                repository,
                new SchedulingPolicy()
        );

        // Act
        Event updated = useCase.execute(
                id,
                "Planning",
                "Nueva descripción",
                original.getStart(),
                original.getEnd()
        );

        // Assert
        assertEquals("Nueva descripción", updated.getDescription());

        // Act + Assert: un cambio inválido (domingo) no modifica lo guardado
        Instant sundayStart = Instant.parse("2026-08-23T14:00:00Z");
        Instant sundayEnd = Instant.parse("2026-08-23T15:00:00Z");
        assertThrows(
                OutsideSchedulingHoursException.class,
                () -> useCase.execute(id, "Planning", "Domingo", sundayStart, sundayEnd)
        );
        assertEquals(
                "Nueva descripción",
                repository.findById(id).orElseThrow().getDescription()
        );
    }
}
