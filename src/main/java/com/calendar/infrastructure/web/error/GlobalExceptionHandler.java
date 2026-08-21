package com.calendar.infrastructure.web.error;

import com.calendar.application.exception.EventNotFoundException;
import com.calendar.application.exception.EventOverlapException;
import com.calendar.application.exception.OutsideSchedulingHoursException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEventNotFound(
            EventNotFoundException exception
    ) {
        return new ApiError(
                "EVENT_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(EventOverlapException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEventOverlap(
            EventOverlapException exception
    ) {
        return new ApiError(
                "EVENT_OVERLAP",
                exception.getMessage()
        );
    }

    @ExceptionHandler(OutsideSchedulingHoursException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleOutsideSchedulingHours(
            OutsideSchedulingHoursException exception
    ) {
        return new ApiError(
                "OUTSIDE_SCHEDULING_HOURS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return new ApiError(
                "INVALID_REQUEST",
                exception.getMessage()
        );
    }
}