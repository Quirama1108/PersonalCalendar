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

        List<Event> sortedEvents = events.stream()
                .filter(event ->
                        event.getStart().isBefore(searchWindow.end())
                                && event.getEnd().isAfter(searchWindow.start())
                )
                .sorted(Comparator.comparing(Event::getStart))
                .toList();

        List<TimeSlot> availableSlots = new ArrayList<>();

        Instant cursor = searchWindow.start();

        for (Event event : sortedEvents) {

            Instant eventStart =
                    event.getStart().isBefore(searchWindow.start())
                            ? searchWindow.start()
                            : event.getStart();

            Instant eventEnd =
                    event.getEnd().isAfter(searchWindow.end())
                            ? searchWindow.end()
                            : event.getEnd();

            if (!eventEnd.isAfter(cursor)) {
                continue;
            }

            if (cursor.isBefore(eventStart)) {

                TimeSlot freeSlot = new TimeSlot(cursor, eventStart);

                if (freeSlot.canFit(requiredDuration)) {
                    availableSlots.add(freeSlot);
                }
            }

            if (eventEnd.isAfter(cursor)) {
                cursor = eventEnd;
            }

            if (!cursor.isBefore(searchWindow.end())) {
                break;
            }
        }

        if (cursor.isBefore(searchWindow.end())) {

            TimeSlot freeSlot =
                    new TimeSlot(cursor, searchWindow.end());

            if (freeSlot.canFit(requiredDuration)) {
                availableSlots.add(freeSlot);
            }
        }

        return availableSlots;
    }
}