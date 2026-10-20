package com.calendar.infrastructure.web;

import com.calendar.application.usecase.FindAvailabilityUseCase;
import com.calendar.domain.SchedulingPolicy;
import com.calendar.infrastructure.web.dto.AvailabilityResponse;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    private static final ZoneId ZONE = SchedulingPolicy.ZONE;

    private final FindAvailabilityUseCase findAvailabilityUseCase;

    public AvailabilityController(
            FindAvailabilityUseCase findAvailabilityUseCase
    ) {
        this.findAvailabilityUseCase = findAvailabilityUseCase;
    }

    @GetMapping
    public List<AvailabilityResponse> findAvailability(
            @RequestParam LocalDate date,
            @RequestParam long durationMinutes
    ) {

        return findAvailabilityUseCase.execute(
                        date,
                        Duration.ofMinutes(durationMinutes)
                )
                .stream()
                .map(slot -> new AvailabilityResponse(
                        slot.start()
                                .atZone(ZONE)
                                .toOffsetDateTime(),
                        slot.end()
                                .atZone(ZONE)
                                .toOffsetDateTime()
                ))
                .toList();
    }
}