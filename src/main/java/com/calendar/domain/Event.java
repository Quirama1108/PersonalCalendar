package com.calendar.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Event {

    private final UUID id;
    private final String title;
    private final Instant start;
    private final Instant end;

    public Event(UUID id, String title, Instant start, Instant end) {

        Objects.requireNonNull(id, "Event id cannot be null");
        Objects.requireNonNull(title, "Event title cannot be null");
        Objects.requireNonNull(start, "Event start cannot be null");
        Objects.requireNonNull(end, "Event end cannot be null");

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Event start must be before event end"
            );
        }

        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public boolean overlaps(Event other) {
        Objects.requireNonNull(other, "Other event cannot be null");

        return start.isBefore(other.end)
                && end.isAfter(other.start);
    }
}