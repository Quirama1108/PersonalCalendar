package com.calendar.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AvailabilityCalculator {

    public List<TimeSlot> findAvailableSlots(
            TimeSlot searchWindow,
            Duration requiredDuration,
            List<Event> events
    ) {
        Objects.requireNonNull(searchWindow);
        Objects.requireNonNull(requiredDuration);
        Objects.requireNonNull(events);

        if (requiredDuration.isZero() || requiredDuration.isNegative()) {
            throw new IllegalArgumentException(
                    "Required duration must be greater than zero"
            );
        }

        List<TimeSlot> availableSlots = new ArrayList<>();
        Instant cursor = searchWindow.start();

        // Los eventos se recorren en orden; el cursor marca hasta dónde está ocupado.
        for (Event event : eventsWithin(searchWindow, events)) {
            addIfFits(availableSlots, cursor, event.getStart(), requiredDuration);
            cursor = latest(cursor, event.getEnd());
        }

        addIfFits(availableSlots, cursor, searchWindow.end(), requiredDuration);

        return availableSlots;
    }

    private static List<Event> eventsWithin(TimeSlot window, List<Event> events) {
        return events.stream()
                .filter(event ->
                        event.getStart().isBefore(window.end())
                                && event.getEnd().isAfter(window.start())
                )
                .sorted(Comparator.comparing(Event::getStart))
                .toList();
    }

    private static void addIfFits(
            List<TimeSlot> slots,
            Instant from,
            Instant to,
            Duration requiredDuration
    ) {
        if (!from.isBefore(to)) {
            return;
        }

        TimeSlot gap = new TimeSlot(from, to);

        if (gap.canFit(requiredDuration)) {
            slots.add(gap);
        }
    }

    private static Instant latest(Instant first, Instant second) {
        return first.isAfter(second) ? first : second;
    }
}
