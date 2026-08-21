package com.calendar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
public class EventJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_time", nullable = false)
    private Instant start;

    @Column(name = "end_time", nullable = false)
    private Instant end;

    protected EventJpaEntity() {
        // Required by JPA
    }

    public EventJpaEntity(
            UUID id,
            String title,
            Instant start,
            Instant end
    ) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }
}