package com.calendar.application.usecase;

import com.calendar.domain.Event;
import com.calendar.support.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GetEventsUseCaseTest {

    @Test
    void shouldReturnEventsInsideRequestedRangeOrderedByStart() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        Event laterEvent = new Event(
                UUID.randomUUID(),
                "Later",
                Instant.parse("2026-08-24T17:00:00Z"),
                Instant.parse("2026-08-24T18:00:00Z")
        );

        Event earlierEvent = new Event(
                UUID.randomUUID(),
                "Earlier",
                Instant.parse("2026-08-24T15:00:00Z"),
                Instant.parse("2026-08-24T16:00:00Z")
        );

        repository.save(laterEvent);
        repository.save(earlierEvent);

        GetEventsUseCase useCase = new GetEventsUseCase(repository);

        // Act
        List<Event> result = useCase.execute(
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T22:00:00Z")
        );

        // Assert
        assertEquals(2, result.size());
        assertEquals("Earlier", result.get(0).getTitle());
        assertEquals("Later", result.get(1).getTitle());
    }

    @Test
    void shouldRejectInvalidRange() {

        // Arrange
        GetEventsUseCase useCase =
                new GetEventsUseCase(new InMemoryEventRepository());

        Instant date = Instant.parse("2026-08-24T14:00:00Z");

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(date, date)
        );

        // Assert
        assertEquals(
                "Start must be before end",
                exception.getMessage()
        );
    }
}