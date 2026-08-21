package com.calendar.infrastructure.web.dto;

import java.time.OffsetDateTime;

public record EventRequest(
        String title,
        OffsetDateTime start,
        OffsetDateTime end
) {
}