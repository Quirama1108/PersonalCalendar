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

        UUID id = UUID.randomUUID();

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Event(
                                id,
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

        UUID id = UUID.randomUUID();

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Event(
                                id,
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

    private static final Instant START = Instant.parse("2026-08-24T14:00:00Z");
    private static final Instant END = Instant.parse("2026-08-24T15:00:00Z");

    @Test
    void shouldRejectBlankOrMissingTitle() {

        // Arrange
        UUID id = UUID.randomUUID();

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> new Event(id, "   ", START, END));
        assertThrows(IllegalArgumentException.class,
                () -> new Event(id, null, START, END));
    }

    @Test
    void shouldTrimTitleAndDescriptionAndDefaultDescriptionToEmpty() {

        // Act
        Event withDescription = new Event(
                UUID.randomUUID(), "  Demo  ", "  Sala 2  ", START, END
        );
        Event withoutDescription = new Event(
                UUID.randomUUID(), "Demo", null, START, END
        );

        // Assert
        assertEquals("Demo", withDescription.getTitle());
        assertEquals("Sala 2", withDescription.getDescription());
        assertEquals("", withoutDescription.getDescription());
    }

    @Test
    void shouldRejectTitleOrDescriptionAboveMaximumLength() {

        // Arrange
        String longTitle = "a".repeat(Event.MAX_TITLE_LENGTH + 1);
        String longDescription = "a".repeat(Event.MAX_DESCRIPTION_LENGTH + 1);
        String maxTitle = "a".repeat(Event.MAX_TITLE_LENGTH);
        String maxDescription = "a".repeat(Event.MAX_DESCRIPTION_LENGTH);
        UUID id = UUID.randomUUID();

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> new Event(id, longTitle, START, END));
        assertThrows(IllegalArgumentException.class,
                () -> new Event(id, "Demo", longDescription, START, END));
        assertDoesNotThrow(() -> new Event(id, maxTitle, maxDescription, START, END));
    }
}
