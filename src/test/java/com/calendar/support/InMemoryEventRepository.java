package com.calendar.support;

import com.calendar.application.port.EventRepository;
import com.calendar.domain.Event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InMemoryEventRepository implements EventRepository {

    private final List<Event> events = new ArrayList<>();

    @Override
    public Event save(Event event) {
        events.removeIf(existing ->
                existing.getId().equals(event.getId())
        );

        events.add(event);
        return event;
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return events.stream()
                .filter(event -> event.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Event> findBetween(Instant start, Instant end) {
        return events.stream()
                .filter(event ->
                        event.getStart().isBefore(end)
                                && event.getEnd().isAfter(start)
                )
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        events.removeIf(event ->
                event.getId().equals(id)
        );
    }

    public List<Event> findAll() {
        return List.copyOf(events);
    }
}