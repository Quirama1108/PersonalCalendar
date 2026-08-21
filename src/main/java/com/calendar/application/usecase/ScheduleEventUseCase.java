package com.calendar.application.usecase;

import com.calendar.application.port.EventRepository;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;
import com.calendar.application.exception.EventOverlapException;
import com.calendar.application.exception.OutsideSchedulingHoursException;

import java.time.Instant;
import java.util.List;
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

        Event event = new Event(
                UUID.randomUUID(),
                title,
                start,
                end
        );

        if (!schedulingPolicy.isAllowed(start, end)) {
            throw new OutsideSchedulingHoursException();
        }

        List<Event> existingEvents =
                eventRepository.findBetween(start, end);

        boolean hasConflict = existingEvents.stream()
                .anyMatch(existing -> existing.overlaps(event));

        if (hasConflict) {
            throw new EventOverlapException();
        }

        return eventRepository.save(event);
    }
}