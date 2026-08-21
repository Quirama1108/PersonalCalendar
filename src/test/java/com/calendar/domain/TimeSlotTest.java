package com.calendar.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    @Test
    void shouldCalculateDuration() {
        // Arrange
        TimeSlot slot = new TimeSlot(
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:30:00Z")
        );

        // Act
        Duration duration = slot.duration();

        // Assert
        assertEquals(Duration.ofMinutes(90), duration);
    }

    @Test
    void shouldFitRequiredDuration() {
        // Arrange
        TimeSlot slot = new TimeSlot(
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:30:00Z")
        );

        // Act
        boolean canFit = slot.canFit(Duration.ofMinutes(60));

        // Assert
        assertTrue(canFit);
    }

    @Test
    void shouldNotFitLongerDuration() {
        // Arrange
        TimeSlot slot = new TimeSlot(
                Instant.parse("2026-08-24T14:00:00Z"),
                Instant.parse("2026-08-24T15:00:00Z")
        );

        // Act
        boolean canFit = slot.canFit(Duration.ofMinutes(90));

        // Assert
        assertFalse(canFit);
    }

    @Test
    void shouldRejectInvalidRange() {
        // Arrange
        Instant date = Instant.parse("2026-08-24T14:00:00Z");

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TimeSlot(date, date)
        );

        // Assert
        assertEquals(
                "Time slot start must be before end",
                exception.getMessage()
        );
    }
}