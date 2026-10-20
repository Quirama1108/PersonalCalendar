package com.calendar.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SchedulingPolicyTest {

    private final SchedulingPolicy policy = new SchedulingPolicy();

    // Las horas están en UTC; el comentario indica la hora de Colombia (UTC-5).
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Weekday within business hours, 2026-08-24T14:00:00Z, 2026-08-24T15:00:00Z", // lun 09:00-10:00
            "Whole workday,                 2026-08-24T14:00:00Z, 2026-08-24T22:00:00Z", // lun 09:00-17:00
            "Saturday until four,           2026-08-22T19:00:00Z, 2026-08-22T21:00:00Z"  // sáb 14:00-16:00
    })
    void shouldAllowEventsWithinSchedulingHours(String scenario, Instant start, Instant end) {
        // Act + Assert
        assertTrue(policy.isAllowed(start, end));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Weekday before nine,             2026-08-24T13:30:00Z, 2026-08-24T14:30:00Z", // lun 08:30-09:30
            "One minute before opening,       2026-08-24T13:59:00Z, 2026-08-24T15:00:00Z", // lun 08:59-10:00
            "Weekday after five,              2026-08-24T20:00:00Z, 2026-08-24T22:30:00Z", // lun 15:00-17:30
            "Saturday after four,             2026-08-22T20:00:00Z, 2026-08-22T21:30:00Z", // sáb 15:00-16:30
            "Sunday,                          2026-08-23T15:00:00Z, 2026-08-23T16:00:00Z", // dom 10:00-11:00
            "Crossing into next day,          2026-08-24T21:30:00Z, 2026-08-25T14:30:00Z"  // lun 16:30 - mar 09:30
    })
    void shouldRejectEventsOutsideSchedulingHours(String scenario, Instant start, Instant end) {
        // Act + Assert
        assertFalse(policy.isAllowed(start, end));
    }
}
