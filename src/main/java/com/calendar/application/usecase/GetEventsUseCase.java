package com.calendar.application.usecase;

import com.calendar.application.port.EventRepository;
import com.calendar.domain.Event;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GetEventsUseCase {

    private final EventRepository eventRepository;

    public GetEventsUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> execute(Instant start, Instant end) {
        Objects.requireNonNull(start, "Start cannot be null");
        Objects.requireNonNull(end, "End cannot be null");

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Start must be before end"
            );
        }

        return eventRepository.findBetween(start, end)
                .stream()
                .sorted(Comparator.comparing(Event::getStart))
                .toList();
    }
}