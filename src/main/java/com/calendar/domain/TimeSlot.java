package com.calendar.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record TimeSlot(
        Instant start,
        Instant end
) {

    public TimeSlot {
        Objects.requireNonNull(start, "Time slot start cannot be null");
        Objects.requireNonNull(end, "Time slot end cannot be null");

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Time slot start must be before end"
            );
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    public boolean canFit(Duration requiredDuration) {
        Objects.requireNonNull(requiredDuration, "Required duration cannot be null");

        return duration().compareTo(requiredDuration) >= 0;
    }
}