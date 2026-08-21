package com.calendar.application.usecase;

import com.calendar.application.port.EventRepository;
import com.calendar.application.exception.EventNotFoundException;

import java.util.Objects;
import java.util.UUID;

public class DeleteEventUseCase {

    private final EventRepository eventRepository;

    public DeleteEventUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void execute(UUID id) {
        Objects.requireNonNull(id, "Event id cannot be null");

        boolean exists = eventRepository.findById(id).isPresent();

        if (!exists) {
            throw new EventNotFoundException();
        }

        eventRepository.deleteById(id);
    }
}