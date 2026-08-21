package com.calendar.infrastructure.web.error;

public record ApiError(
        String code,
        String message
) {
}