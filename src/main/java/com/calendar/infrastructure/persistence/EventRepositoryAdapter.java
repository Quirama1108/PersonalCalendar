package com.calendar.infrastructure.persistence;

import com.calendar.application.port.EventRepository;
import com.calendar.domain.Event;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EventRepositoryAdapter implements EventRepository {

    private final SpringDataEventRepository repository;

    public EventRepositoryAdapter(
            SpringDataEventRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Event save(Event event) {

        EventJpaEntity entity = toEntity(event);

        EventJpaEntity saved = repository.save(entity);

        return toDomain(saved);
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public List<Event> findBetween(
            Instant start,
            Instant end
    ) {
        return repository
                .findByStartLessThanAndEndGreaterThan(
                        end,
                        start
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private EventJpaEntity toEntity(Event event) {
        return new EventJpaEntity(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStart(),
                event.getEnd()
        );
    }

    private Event toDomain(EventJpaEntity entity) {
        return new Event(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStart(),
                entity.getEnd()
        );
    }
}