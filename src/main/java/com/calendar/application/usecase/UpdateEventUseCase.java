package com.calendar.application.usecase;

import com.calendar.application.exception.EventNotFoundException;
import com.calendar.application.port.EventRepository;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;

import java.time.Instant;
import java.util.UUID;

public class UpdateEventUseCase {

    private final EventRepository eventRepository;
    private final SchedulingPolicy schedulingPolicy;

    public UpdateEventUseCase(
            EventRepository eventRepository,
            SchedulingPolicy schedulingPolicy
    ) {
        this.eventRepository = eventRepository;
        this.schedulingPolicy = schedulingPolicy;
    }

    public Event execute(
            UUID id,
            String title,
            Instant start,
            Instant end
    ) {
        return execute(id, title, "", start, end);
    }

    public Event execute(
            UUID id,
            String title,
            String description,
            Instant start,
            Instant end
    ) {

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(EventNotFoundException::new);

        Event updatedEvent = new Event(
                existingEvent.getId(),
                title,
                description,
                start,
                end
        );

        SchedulingGuard.ensureCanBeScheduled(
                updatedEvent,
                schedulingPolicy,
                eventRepository
        );

        return eventRepository.save(updatedEvent);
    }
}
