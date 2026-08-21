package com.calendar.benchmark;

import com.calendar.domain.AvailabilityCalculator;
import com.calendar.domain.Event;
import com.calendar.domain.TimeSlot;
import org.openjdk.jmh.annotations.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@Fork(1)
public class AvailabilityCalculatorBenchmark {

    @Param({
            "10",
            "100",
            "1000",
            "10000"
    })
    private int eventCount;

    private AvailabilityCalculator calculator;
    private TimeSlot searchWindow;
    private Duration requiredDuration;
    private List<Event> events;

    @Setup
    public void setup() {

        calculator = new AvailabilityCalculator();

        Instant start =
                Instant.parse("2026-08-24T14:00:00Z");

        searchWindow = new TimeSlot(
                start,
                start.plus(Duration.ofDays(365))
        );

        requiredDuration = Duration.ofMinutes(30);

        events = new ArrayList<>();

        Instant cursor = start.plusSeconds(60);

        for (int i = 0; i < eventCount; i++) {

            events.add(
                    new Event(
                            UUID.randomUUID(),
                            "Event " + i,
                            cursor,
                            cursor.plusSeconds(60)
                    )
            );

            cursor = cursor.plusSeconds(120);
        }

        Collections.shuffle(events);
    }

    @Benchmark
    public List<TimeSlot> calculateAvailability() {
        return calculator.findAvailableSlots(
                searchWindow,
                requiredDuration,
                events
        );
    }
}