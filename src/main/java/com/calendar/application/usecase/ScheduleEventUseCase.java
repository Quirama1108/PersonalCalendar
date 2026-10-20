package com.calendar.application.usecase;

import com.calendar.application.port.EventRepository;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;

import java.time.Instant;
import java.util.UUID;

public class ScheduleEventUseCase {

    private final EventRepository eventRepository;
    private final SchedulingPolicy schedulingPolicy;

    public ScheduleEventUseCase(
            EventRepository eventRepository,
            SchedulingPolicy schedulingPolicy
    ) {
        this.eventRepository = eventRepository;
        this.schedulingPolicy = schedulingPolicy;
    }

    public Event execute(
            String title,
            Instant start,
            Instant end
    ) {
        return execute(title, "", start, end);
    }

    public Event execute(
            String title,
            String description,
            Instant start,
            Instant end
    ) {

        Event event = new Event(
                UUID.randomUUID(),
                title,
                description,
                start,
                end
        );

        SchedulingGuard.ensureCanBeScheduled(
                event,
                schedulingPolicy,
                eventRepository
        );

        return eventRepository.save(event);
    }
}
