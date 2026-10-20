package com.calendar.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Event {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private final UUID id;
    private final String title;
    private final String description;
    private final Instant start;
    private final Instant end;

    public Event(UUID id, String title, Instant start, Instant end) {
        this(id, title, "", start, end);
    }

    public Event(
            UUID id,
            String title,
            String description,
            Instant start,
            Instant end
    ) {

        Objects.requireNonNull(id, "Event id cannot be null");
        Objects.requireNonNull(start, "Event start cannot be null");
        Objects.requireNonNull(end, "Event end cannot be null");

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Event start must be before event end"
            );
        }

        this.id = id;
        this.title = withMaxLength(requireNotBlank(title), MAX_TITLE_LENGTH, "title");
        this.description = withMaxLength(
                description == null ? "" : description.strip(),
                MAX_DESCRIPTION_LENGTH,
                "description"
        );
        this.start = start;
        this.end = end;
    }

    private static String requireNotBlank(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Event title cannot be blank");
        }

        return title.strip();
    }

    private static String withMaxLength(String value, int maxLength, String field) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    "Event " + field + " must be at most " + maxLength + " characters"
            );
        }

        return value;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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