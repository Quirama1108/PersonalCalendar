package com.calendar.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataEventRepository
        extends JpaRepository<EventJpaEntity, UUID> {

    List<EventJpaEntity> findByStartLessThanAndEndGreaterThan(
            Instant end,
            Instant start
    );
}