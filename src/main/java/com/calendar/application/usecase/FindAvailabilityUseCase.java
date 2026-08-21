package com.calendar.application.usecase;

import com.calendar.application.port.EventRepository;
import com.calendar.domain.AvailabilityCalculator;
import com.calendar.domain.Event;
import com.calendar.domain.SchedulingPolicy;
import com.calendar.domain.TimeSlot;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FindAvailabilityUseCase {

    private final EventRepository eventRepository;
    private final SchedulingPolicy schedulingPolicy;
    private final AvailabilityCalculator availabilityCalculator;

    public FindAvailabilityUseCase(
            EventRepository eventRepository,
            SchedulingPolicy schedulingPolicy,
            AvailabilityCalculator availabilityCalculator
    ) {
        this.eventRepository = eventRepository;
        this.schedulingPolicy = schedulingPolicy;
        this.availabilityCalculator = availabilityCalculator;
    }

    public List<TimeSlot> execute(
            LocalDate date,
            Duration requiredDuration
    ) {

        Optional<TimeSlot> availability =
                schedulingPolicy.availabilityFor(date);

        if (availability.isEmpty()) {
            return List.of();
        }

        TimeSlot workingHours = availability.get();

        List<Event> events = eventRepository.findBetween(
                workingHours.start(),
                workingHours.end()
        );

        return availabilityCalculator.findAvailableSlots(
                workingHours,
                requiredDuration,
                events
        );
    }
}