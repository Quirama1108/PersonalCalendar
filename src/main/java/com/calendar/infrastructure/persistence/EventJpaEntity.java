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

    // Nullable para que ddl-auto=update pueda agregar la columna a filas existentes.
    @Column(length = 500)
    private String description;

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
            String description,
            Instant start,
            Instant end
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }
}