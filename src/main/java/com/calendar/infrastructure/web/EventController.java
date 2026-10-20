package com.calendar.infrastructure.web;

import com.calendar.application.usecase.DeleteEventUseCase;
import com.calendar.application.usecase.GetEventsUseCase;
import com.calendar.application.usecase.ScheduleEventUseCase;
import com.calendar.application.usecase.UpdateEventUseCase;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;
import com.calendar.infrastructure.web.dto.EventRequest;
import com.calendar.infrastructure.web.dto.EventResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final ZoneId ZONE = SchedulingPolicy.ZONE;

    private final ScheduleEventUseCase scheduleEventUseCase;
    private final GetEventsUseCase getEventsUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;

    public EventController(
            ScheduleEventUseCase scheduleEventUseCase,
            GetEventsUseCase getEventsUseCase,
            UpdateEventUseCase updateEventUseCase,
            DeleteEventUseCase deleteEventUseCase
    ) {
        this.scheduleEventUseCase = scheduleEventUseCase;
        this.getEventsUseCase = getEventsUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(
            @RequestBody EventRequest request
    ) {

        Event event = scheduleEventUseCase.execute(
                request.title(),
                request.description(),
                required(request.start(), "start"),
                required(request.end(), "end")
        );

        return toResponse(event);
    }

    @GetMapping
    public List<EventResponse> getEvents(
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to
    ) {

        return getEventsUseCase.execute(
                        from.toInstant(),
                        to.toInstant()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public EventResponse update(
            @PathVariable UUID id,
            @RequestBody EventRequest request
    ) {

        Event event = updateEventUseCase.execute(
                id,
                request.title(),
                request.description(),
                required(request.start(), "start"),
                required(request.end(), "end")
        );

        return toResponse(event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        deleteEventUseCase.execute(id);
    }

    private static Instant required(OffsetDateTime value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("Event " + field + " is required");
        }

        return value.toInstant();
    }

    private EventResponse toResponse(Event event) {

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStart()
                        .atZone(ZONE)
                        .toOffsetDateTime(),
                event.getEnd()
                        .atZone(ZONE)
                        .toOffsetDateTime()
        );
    }
}