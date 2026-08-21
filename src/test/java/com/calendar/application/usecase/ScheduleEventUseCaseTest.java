package com.calendar.application.usecase;

import com.calendar.application.exception.EventOverlapException;
import com.calendar.application.exception.OutsideSchedulingHoursException;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;
import com.calendar.support.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleEventUseCaseTest {

    @Test
    void shouldScheduleEventWhenTimeIsValidAndAvailable() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        ScheduleEventUseCase useCase = new ScheduleEventUseCase(
                repository,
                new SchedulingPolicy()
        );

        Instant start = Instant.parse("2026-08-24T14:00:00Z");
        Instant end = Instant.parse("2026-08-24T15:00:00Z");

        // Act
        Event result = useCase.execute(
                "Technical interview",
                start,
                end
        );

        // Assert
        assertNotNull(result.getId());
        assertEquals("Technical interview", result.getTitle());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void shouldRejectEventOutsideAllowedHours() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        ScheduleEventUseCase useCase = new ScheduleEventUseCase(
                repository,
                new SchedulingPolicy()
        );

        Instant start = Instant.parse("2026-08-24T13:00:00Z");
        Instant end = Instant.parse("2026-08-24T14:00:00Z");

        // Act
        OutsideSchedulingHoursException exception = assertThrows(
                OutsideSchedulingHoursException.class,
                () -> useCase.execute(
                        "Too early",
                        start,
                        end
                )
        );

        // Assert
        assertEquals(
                "Event is outside allowed scheduling hours",
                exception.getMessage()
        );

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void shouldRejectOverlappingEvent() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        repository.save(
                new Event(
                        UUID.randomUUID(),
                        "Existing event",
                        Instant.parse("2026-08-24T14:00:00Z"),
                        Instant.parse("2026-08-24T15:00:00Z")
                )
        );

        ScheduleEventUseCase useCase = new ScheduleEventUseCase(
                repository,
                new SchedulingPolicy()
        );

        Instant start = Instant.parse("2026-08-24T14:30:00Z");
        Instant end = Instant.parse("2026-08-24T15:30:00Z");

        // Act
        EventOverlapException exception = assertThrows(
                EventOverlapException.class,
                () -> useCase.execute(
                        "Conflicting event",
                        start,
                        end
                )
        );

        // Assert
        assertEquals(
                "Event overlaps with an existing event",
                exception.getMessage()
        );

        assertEquals(1, repository.findAll().size());
    }
}