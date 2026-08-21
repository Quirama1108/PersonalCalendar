package com.calendar.infrastructure.config;

import com.calendar.application.port.EventRepository;
import com.calendar.application.usecase.DeleteEventUseCase;
import com.calendar.application.usecase.FindAvailabilityUseCase;
import com.calendar.application.usecase.GetEventsUseCase;
import com.calendar.application.usecase.ScheduleEventUseCase;
import com.calendar.application.usecase.UpdateEventUseCase;
import com.calendar.domain.AvailabilityCalculator;
import com.calendar.domain.SchedulingPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public SchedulingPolicy schedulingPolicy() {
        return new SchedulingPolicy();
    }

    @Bean
    public AvailabilityCalculator availabilityCalculator() {
        return new AvailabilityCalculator();
    }

    @Bean
    public ScheduleEventUseCase scheduleEventUseCase(
            EventRepository eventRepository,
            SchedulingPolicy schedulingPolicy
    ) {
        return new ScheduleEventUseCase(
                eventRepository,
                schedulingPolicy
        );
    }

    @Bean
    public GetEventsUseCase getEventsUseCase(
            EventRepository eventRepository
    ) {
        return new GetEventsUseCase(eventRepository);
    }

    @Bean
    public FindAvailabilityUseCase findAvailabilityUseCase(
            EventRepository eventRepository,
            SchedulingPolicy schedulingPolicy,
            AvailabilityCalculator availabilityCalculator
    ) {
        return new FindAvailabilityUseCase(
                eventRepository,
                schedulingPolicy,
                availabilityCalculator
        );
    }

    @Bean
    public DeleteEventUseCase deleteEventUseCase(
            EventRepository eventRepository
    ) {
        return new DeleteEventUseCase(eventRepository);
    }

    @Bean
    public UpdateEventUseCase updateEventUseCase(
            EventRepository eventRepository,
            SchedulingPolicy schedulingPolicy
    ) {
        return new UpdateEventUseCase(
                eventRepository,
                schedulingPolicy
        );
    }
}