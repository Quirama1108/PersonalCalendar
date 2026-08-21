package com.calendar.application.usecase;

import com.calendar.application.exception.EventNotFoundException;
import com.calendar.domain.Event;
import com.calendar.support.InMemoryEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeleteEventUseCaseTest {

    @Test
    void shouldDeleteExistingEvent() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        UUID id = UUID.randomUUID();

        repository.save(
                new Event(
                        id,
                        "Meeting",
                        Instant.parse("2026-08-24T14:00:00Z"),
                        Instant.parse("2026-08-24T15:00:00Z")
                )
        );

        DeleteEventUseCase useCase =
                new DeleteEventUseCase(repository);

        // Act
        useCase.execute(id);

        // Assert
        assertTrue(repository.findById(id).isEmpty());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void shouldRejectDeleteWhenEventDoesNotExist() {

        // Arrange
        InMemoryEventRepository repository = new InMemoryEventRepository();

        DeleteEventUseCase useCase =
                new DeleteEventUseCase(repository);

        UUID id = UUID.randomUUID();

        // Act
        EventNotFoundException exception = assertThrows(
                EventNotFoundException.class,
                () -> useCase.execute(id)
        );

        // Assert
        assertEquals(
                "Event not found",
                exception.getMessage()
        );

        assertTrue(repository.findAll().isEmpty());
    }
}