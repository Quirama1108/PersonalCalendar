package com.calendar.application.usecase;

import com.calendar.application.exception.EventOverlapException;
import com.calendar.application.exception.OutsideSchedulingHoursException;
import com.calendar.application.port.EventRepository;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;

final class SchedulingGuard {

    private SchedulingGuard() {
    }

    static void ensureCanBeScheduled(
            Event event,
            SchedulingPolicy schedulingPolicy,
            EventRepository eventRepository
    ) {
        if (!schedulingPolicy.isAllowed(event.getStart(), event.getEnd())) {
            throw new OutsideSchedulingHoursException();
        }

        // Se excluye el propio evento para que una edición no choque consigo misma.
        boolean hasConflict = eventRepository
                .findBetween(event.getStart(), event.getEnd())
                .stream()
                .filter(existing -> !existing.getId().equals(event.getId()))
                .anyMatch(existing -> existing.overlaps(event));

        if (hasConflict) {
            throw new EventOverlapException();
        }
    }
}
