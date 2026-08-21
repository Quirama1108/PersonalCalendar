package com.calendar.domain;

import java.time.*;
import java.util.Optional;

public class SchedulingPolicy {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private static final LocalTime OPENING_TIME = LocalTime.of(9, 0);
    private static final LocalTime WEEKDAY_CLOSING_TIME = LocalTime.of(17, 0);
    private static final LocalTime SATURDAY_CLOSING_TIME = LocalTime.of(16, 0);

    public boolean isAllowed(Instant start, Instant end) {
        ZonedDateTime localStart = start.atZone(ZONE);
        ZonedDateTime localEnd = end.atZone(ZONE);

        if (!localStart.toLocalDate().equals(localEnd.toLocalDate())) {
            return false;
        }

        DayOfWeek day = localStart.getDayOfWeek();

        if (day == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime startTime = localStart.toLocalTime();
        LocalTime endTime = localEnd.toLocalTime();

        if (startTime.isBefore(OPENING_TIME)) {
            return false;
        }

        LocalTime closingTime =
                day == DayOfWeek.SATURDAY
                        ? SATURDAY_CLOSING_TIME
                        : WEEKDAY_CLOSING_TIME;

        return !endTime.isAfter(closingTime);
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