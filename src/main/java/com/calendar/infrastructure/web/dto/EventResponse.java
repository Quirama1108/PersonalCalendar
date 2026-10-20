package com.calendar.infrastructure.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        OffsetDateTime start,
        OffsetDateTime end
) {
}