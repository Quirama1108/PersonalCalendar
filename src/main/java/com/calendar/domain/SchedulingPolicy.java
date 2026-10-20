package com.calendar.domain;

import java.time.*;
import java.util.Optional;

public class SchedulingPolicy {

    public static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private static final LocalTime OPENING_TIME = LocalTime.of(9, 0);
    private static final LocalTime WEEKDAY_CLOSING_TIME = LocalTime.of(17, 0);
    private static final LocalTime SATURDAY_CLOSING_TIME = LocalTime.of(16, 0);

    public boolean isAllowed(Instant start, Instant end) {
        LocalDate localDate = start.atZone(ZONE).toLocalDate();

        // Un evento que cruza al día siguiente termina después del cierre, por eso queda rechazado.
        return availabilityFor(localDate)
                .map(hours -> !start.isBefore(hours.start())
                        && !end.isAfter(hours.end()))
                .orElse(false);
    }

    public Optional<TimeSlot> availabilityFor(LocalDate date) {

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return Optional.empty();
        }

        LocalTime closingTime =
                date.getDayOfWeek() == DayOfWeek.SATURDAY
                        ? SATURDAY_CLOSING_TIME
                        : WEEKDAY_CLOSING_TIME;

        Instant start = date
                .atTime(OPENING_TIME)
                .atZone(ZONE)
                .toInstant();

        Instant end = date
                .atTime(closingTime)
                .atZone(ZONE)
                .toInstant();

        return Optional.of(new TimeSlot(start, end));
    }
}