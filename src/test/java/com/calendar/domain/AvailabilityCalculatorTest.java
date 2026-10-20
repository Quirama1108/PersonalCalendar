package com.calendar.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private static final TimeSlot WINDOW = new TimeSlot(
            Instant.parse("2026-08-24T14:00:00Z"),
            Instant.parse("2026-08-24T22:00:00Z")
    );

    private static Event event(String start, String end) {
        return new Event(
                UUID.randomUUID(),
                "Event",
                Instant.parse("2026-08-24T" + start + ":00Z"),
                Instant.parse("2026-08-24T" + end + ":00Z")
        );
    }

    private static TimeSlot slot(String start, String end) {
        return new TimeSlot(
                Instant.parse("2026-08-24T" + start + ":00Z"),
                Instant.parse("2026-08-24T" + end + ":00Z")
        );
    }

    @Test
    void shouldClipEventsThatStartBeforeOrEndAfterTheWindow() {

        // Arrange
        List<Event> events = List.of(
                event("12:00", "15:00"),
                event("21:00", "23:00")
        );

        // Act
        List<TimeSlot> result = calculator.findAvailableSlots(
                WINDOW, Duration.ofMinutes(60), events
        );

        // Assert
        assertEquals(List.of(slot("15:00", "21:00")), result);
    }

    @Test
    void shouldIgnoreEventsOutsideTheWindowAndUnsortedInput() {

        // Arrange
        List<Event> events = List.of(
                event("18:00", "19:00"),
                event("22:00", "23:00"),
                event("10:00", "14:00"),
                event("15:00", "16:00")
        );

        // Act
        List<TimeSlot> result = calculator.findAvailableSlots(
                WINDOW, Duration.ofMinutes(60), events
        );

        // Assert
        assertEquals(
                List.of(
                        slot("14:00", "15:00"),
                        slot("16:00", "18:00"),
                        slot("19:00", "22:00")
                ),
                result
        );
    }

    @Test
    void shouldMergeOverlappingAndNestedEvents() {

        // Arrange
        List<Event> events = List.of(
                event("15:00", "18:00"),
                event("16:00", "17:00"),
                event("17:30", "19:00")
        );

        // Act
        List<TimeSlot> result = calculator.findAvailableSlots(
                WINDOW, Duration.ofMinutes(60), events
        );

        // Assert
        assertEquals(
                List.of(slot("14:00", "15:00"), slot("19:00", "22:00")),
                result
        );
    }

    @Test
    void shouldDiscardGapsShorterThanRequiredDuration() {

        // Arrange
        List<Event> events = List.of(
                event("14:30", "15:00"),
                event("15:45", "21:30")
        );

        // Act
        List<TimeSlot> result = calculator.findAvailableSlots(
                WINDOW, Duration.ofMinutes(60), events
        );

        // Assert
        assertEquals(List.of(), result);
    }

    @Test
    void shouldReturnNothingWhenWindowIsFullyBooked() {

        // Act
        List<TimeSlot> result = calculator.findAvailableSlots(
                WINDOW, Duration.ofMinutes(15), List.of(event("13:00", "23:00"))
        );

        // Assert
        assertEquals(List.of(), result);
    }

    @Test
    void shouldRejectNonPositiveDuration() {

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.findAvailableSlots(WINDOW, Duration.ZERO, List.of())
        );
    }
}
