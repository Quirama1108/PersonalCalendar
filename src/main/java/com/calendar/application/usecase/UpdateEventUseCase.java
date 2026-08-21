package com.calendar.application.usecase;

import com.calendar.application.exception.EventNotFoundException;
import com.calendar.application.exception.EventOverlapException;
import com.calendar.application.exception.OutsideSchedulingHoursException;
import com.calendar.application.port.EventRepository;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;

import java.time.Instant;
import java.util.List;
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

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(EventNotFoundException::new);

        Event updatedEvent = new Event(
                existingEvent.getId(),
                title,
                start,
                end
        );

        if (!schedulingPolicy.isAllowed(start, end)) {
            throw new OutsideSchedulingHoursException();
        }

        List<Event> conflictingEvents =
                eventRepository.findBetween(start, end);

        boolean hasConflict = conflictingEvents.stream()
                .filter(event -> !event.getId().equals(id))
                .anyMatch(event -> event.overlaps(updatedEvent));

        if (hasConflict) {
            throw new EventOverlapException();
        }

        return eventRepository.save(updatedEvent);
    }
}