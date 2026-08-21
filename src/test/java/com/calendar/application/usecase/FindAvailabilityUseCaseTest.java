package com.calendar.application.usecase;

import com.calendar.domain.AvailabilityCalculator;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;
import com.calendar.domain.TimeSlot;
import com.calendar.support.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FindAvailabilityUseCaseTest {

    @Test
    void shouldReturnAvailableSlotsForWeekday() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        repository.save(new Event(
                UUID.randomUUID(),
                "Meeting",
                Instant.parse("2026-08-24T15:00:00Z"),
                Instant.parse("2026-08-24T16:00:00Z")
        ));

        FindAvailabilityUseCase useCase = new FindAvailabilityUseCase(
                repository,
                new SchedulingPolicy(),
                new AvailabilityCalculator()
        );

        // Act
        List<TimeSlot> result = useCase.execute(
                LocalDate.of(2026, 8, 24),
                Duration.ofMinutes(60)
        );

        // Assert
        assertEquals(2, result.size());

        assertEquals(
                Instant.parse("2026-08-24T14:00:00Z"),
                result.get(0).start()
        );

        assertEquals(
                Instant.parse("2026-08-24T15:00:00Z"),
                result.get(0).end()
        );

        assertEquals(
                Instant.parse("2026-08-24T16:00:00Z"),
                result.get(1).start()
        );

        assertEquals(
                Instant.parse("2026-08-24T22:00:00Z"),
                result.get(1).end()
        );
    }

    @Test
    void shouldReturnEmptyAvailabilityOnSunday() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        FindAvailabilityUseCase useCase = new FindAvailabilityUseCase(
                repository,
                new SchedulingPolicy(),
                new AvailabilityCalculator()
        );

        // Act
        List<TimeSlot> result = useCase.execute(
                LocalDate.of(2026, 8, 23),
                Duration.ofMinutes(60)
        );

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRespectSaturdayClosingTime() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        FindAvailabilityUseCase useCase = new FindAvailabilityUseCase(
                repository,
                new SchedulingPolicy(),
                new AvailabilityCalculator()
        );

        // Act
        List<TimeSlot> result = useCase.execute(
                LocalDate.of(2026, 8, 22),
                Duration.ofMinutes(60)
        );

        // Assert
        assertEquals(1, result.size());

        assertEquals(
                Instant.parse("2026-08-22T14:00:00Z"),
                result.getFirst().start()
        );

        assertEquals(
                Instant.parse("2026-08-22T21:00:00Z"),
                result.getFirst().end()
        );
    }
}