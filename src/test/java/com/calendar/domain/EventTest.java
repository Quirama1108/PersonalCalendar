package com.calendar.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void shouldCreateEventWhenDatesAreValid() {

        // Arrange
        UUID id = UUID.randomUUID();

        Instant start =
                Instant.parse("2026-08-24T14:00:00Z");

        Instant end =
                Instant.parse("2026-08-24T15:00:00Z");

        // Act
        Event event = new Event(
                id,
                "Technical interview",
                start,
                end
        );

        // Assert
        assertEquals(id, event.getId());
        assertEquals("Technical interview", event.getTitle());
        assertEquals(start, event.getStart());
        assertEquals(end, event.getEnd());
    }

    @Test
    void shouldRejectEventWhenStartEqualsEnd() {

        // Arrange
        Instant date =
                Instant.parse("2026-08-24T14:00:00Z");

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Event(
                                UUID.randomUUID(),
                                "Invalid event",
                                date,
                                date
                        )
                );

        // Assert
        assertEquals(
                "Event start must be before event end",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectEventWhenEndIsBeforeStart() {

        // Arrange
        Instant start =
                Instant.parse("2026-08-24T15:00:00Z");

        Instant end =
                Instant.parse("2026-08-24T14:00:00Z");

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Event(
                                UUID.randomUUID(),
                                "Invalid event",
                                start,
                                end
                        )
                );

        // Assert
        assertEquals(
                "Event start must be before event end",
                exception.getMessage()
        );
    }

    @Test
    void shouldDetectOverlappingEvents() {

        // Arrange
        Event first = new Event(
                UUID.randomUUID(),
                "First event",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        );

        Event second = new Event(
                UUID.randomUUID(),
                "Second event",
                Instant.parse("2026-08-24T14:30:00Z"),
                Instant.parse("2026-08-24T15:30:00Z")
        );

        // Act
        boolean overlaps = first.overlaps(second);

        // Assert
        assertTrue(overlaps);
    }

    @Test
    void shouldNotOverlapWhenOneEventStartsExactlyWhenAnotherEnds() {

        // Arrange
        Event first = new Event(
                UUID.randomUUID(),
                "First event",
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        );

        Event second = new Event(
                UUID.randomUUID(),
                "Second event",
                Instant.parse("2026-08-24T15:00:00Z"),
                Instant.parse("2026-08-24T16:00:00Z")
        );

        // Act
        boolean overlaps = first.overlaps(second);

        // Assert
        assertFalse(overlaps);
    }
}