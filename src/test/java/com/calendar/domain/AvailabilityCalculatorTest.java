package com.calendar.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvailabilityCalculatorTest {

    private final AvailabilityCalculator calculator =
            new AvailabilityCalculator();

    @Test
    void shouldReturnEntireWindowWhenThereAreNoEvents() {

        // Arrange
        TimeSlot window = new TimeSlot(
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T22:00:00Z")
        );

        // Act
        List<TimeSlot> result = calculator.findAvailableSlots(
                window,
                Duration.ofMinutes(60),
                List.of()
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals(window, result.getFirst());
    }

    @Test
    void shouldFindAvailableSlotsBetweenEvents() {

        // Arrange
        TimeSlot window = new TimeSlot(
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T22:00:00Z")
        );

        Event first = new Event(
                UUID.randomUUID(),
                "First",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:30:00Z")
        );

        Event second = new Event(
                UUID.randomUUID(),
                "Second",
                Instant.parse("2026-08-24T17:00:00Z"),
                Instant.parse("2026-08-24T18:00:00Z")
        );

        Event third = new Event(
                UUID.randomUUID(),
                "Third",
                Instant.parse("2026-08-24T20:00:00Z"),
                Instant.parse("2026-08-24T21:00:00Z")
        );

        // Act
        List<TimeSlot> result = calculator.findAvailableSlots(
                window,
                Duration.ofMinutes(60),
                List.of(first, second, third)
        );

        // Assert
        assertEquals(3, result.size());

        assertEquals(
                new TimeSlot(
                        Instant.parse("2026-08-24T15:30:00Z"),
                        Instant.parse("2026-08-24T17:00:00Z")
                ),
                result.get(0)
        );

        assertEquals(
                new TimeSlot(
                        Instant.parse("2026-08-24T18:00:00Z"),
                        Instant.parse("2026-08-24T20:00:00Z")
                ),
                result.get(1)
        );

        assertEquals(
                new TimeSlot(
                        Instant.parse("2026-08-24T21:00:00Z"),
                        Instant.parse("2026-08-24T22:00:00Z")
                ),
                result.get(2)
        );
    }
}