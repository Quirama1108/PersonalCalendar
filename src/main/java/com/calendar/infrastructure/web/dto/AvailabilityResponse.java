package com.calendar.infrastructure.web.dto;

import java.time.OffsetDateTime;

public record AvailabilityResponse(
        OffsetDateTime start,
        OffsetDateTime end
) {
}