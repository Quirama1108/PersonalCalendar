package com.calendar.application.port;

import com.calendar.domain.Event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {

    Event save(Event event);

    Optional<Event> findById(UUID id);

    List<Event> findBetween(Instant start, Instant end);

    void deleteById(UUID id);
}