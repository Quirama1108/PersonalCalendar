package com.calendar.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SchedulingPolicyTest {

    private final SchedulingPolicy policy = new SchedulingPolicy();

    @Test
    void shouldAllowWeekdayEventWithinBusinessHours() {
        // Arrange
        Instant start = Instant.parse("2026-08-24T14:00:00Z"); // 09:00 Colombia
        Instant end = Instant.parse("2026-08-24T15:00:00Z");   // 10:00 Colombia

        // Act
        boolean allowed = policy.isAllowed(start, end);

        // Assert
        assertTrue(allowed);
    }

    @Test
    void shouldRejectWeekdayEventBeforeNine() {
        // Arrange
        Instant start = Instant.parse("2026-08-24T13:30:00Z"); // 08:30
        Instant end = Instant.parse("2026-08-24T14:30:00Z");

        // Act
        boolean allowed = policy.isAllowed(start, end);

        // Assert
        assertFalse(allowed);
    }

    @Test
    void shouldRejectWeekdayEventAfterFive() {
        // Arrange
        Instant start = Instant.parse("2026-08-24T20:00:00Z"); // 15:00
        Instant end = Instant.parse("2026-08-24T22:30:00Z");   // 17:30

        // Act
        boolean allowed = policy.isAllowed(start, end);

        // Assert
        assertFalse(allowed);
    }

    @Test
    void shouldAllowSaturdayUntilFour() {
        // Arrange
        Instant start = Instant.parse("2026-08-22T19:00:00Z"); // 14:00
        Instant end = Instant.parse("2026-08-22T21:00:00Z");   // 16:00

        // Act
        boolean allowed = policy.isAllowed(start, end);

        // Assert
        assertTrue(allowed);
    }

    @Test
    void shouldRejectSaturdayAfterFour() {
        // Arrange
        Instant start = Instant.parse("2026-08-22T20:00:00Z"); // 15:00
        Instant end = Instant.parse("2026-08-22T21:30:00Z");   // 16:30

        // Act
        boolean allowed = policy.isAllowed(start, end);

        // Assert
        assertFalse(allowed);
    }

    @Test
    void shouldRejectSunday() {
        // Arrange
        Instant start = Instant.parse("2026-08-23T15:00:00Z"); // 10:00 Sunday
        Instant end = Instant.parse("2026-08-23T16:00:00Z");

        // Act
        boolean allowed = policy.isAllowed(start, end);

        // Assert
        assertFalse(allowed);
    }

    @Test
    void shouldRejectEventCrossingIntoNextDay() {
        // Arrange
        Instant start = Instant.parse("2026-08-24T21:30:00Z");
        Instant end = Instant.parse("2026-08-25T14:30:00Z");

        // Act
        boolean allowed = policy.isAllowed(start, end);

        // Assert
        assertFalse(allowed);
    }
}