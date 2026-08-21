package com.calendar.integration;

import com.calendar.infrastructure.persistence.SpringDataEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalendarIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataEventRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndConsultEvent() throws Exception {

        // Arrange
        String request = """
                {
                    "title": "Technical interview",
                    "start": "2026-08-24T09:00:00-05:00",
                    "end": "2026-08-24T10:00:00-05:00"
                }
                """;

        // Act + Assert - Create
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title")
                        .value("Technical interview"));

        // Act + Assert - Consult
        mockMvc.perform(get("/events")
                        .param(
                                "from",
                                "2026-08-24T09:00:00-05:00"
                        )
                        .param(
                                "to",
                                "2026-08-24T17:00:00-05:00"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("Technical interview"));
    }

    @Test
    void shouldRejectEventOutsideSchedulingHours() throws Exception {

        // Arrange
        String request = """
                {
                    "title": "Too early",
                    "start": "2026-08-24T08:00:00-05:00",
                    "end": "2026-08-24T09:00:00-05:00"
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("OUTSIDE_SCHEDULING_HOURS"));
    }

    @Test
    void shouldRejectOverlappingEvent() throws Exception {

        // Arrange
        String firstEvent = """
                {
                    "title": "Meeting A",
                    "start": "2026-08-24T10:00:00-05:00",
                    "end": "2026-08-24T11:00:00-05:00"
                }
                """;

        String overlappingEvent = """
                {
                    "title": "Meeting B",
                    "start": "2026-08-24T10:30:00-05:00",
                    "end": "2026-08-24T11:30:00-05:00"
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstEvent))
                .andExpect(status().isCreated());

        // Act + Assert
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overlappingEvent))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("EVENT_OVERLAP"));
    }

    @Test
    void shouldReturnAvailabilityAroundExistingEvent() throws Exception {

        // Arrange
        String event = """
                {
                    "title": "Meeting",
                    "start": "2026-08-24T10:00:00-05:00",
                    "end": "2026-08-24T11:00:00-05:00"
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event))
                .andExpect(status().isCreated());

        // Act + Assert
        mockMvc.perform(get("/availability")
                        .param("date", "2026-08-24")
                        .param("durationMinutes", "60"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$[0].start")
                        .value("2026-08-24T09:00:00-05:00"))
                .andExpect(jsonPath("$[0].end")
                        .value("2026-08-24T10:00:00-05:00"))

                .andExpect(jsonPath("$[1].start")
                        .value("2026-08-24T11:00:00-05:00"))
                .andExpect(jsonPath("$[1].end")
                        .value("2026-08-24T17:00:00-05:00"));
    }

    @Test
    void shouldUpdateExistingEvent() throws Exception {

        // Arrange
        String createRequest = """
            {
                "title": "Original meeting",
                "start": "2026-08-24T09:00:00-05:00",
                "end": "2026-08-24T10:00:00-05:00"
            }
            """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated());

        var id = repository.findAll().getFirst().getId();

        String updateRequest = """
            {
                "title": "Updated meeting",
                "start": "2026-08-24T11:00:00-05:00",
                "end": "2026-08-24T12:00:00-05:00"
            }
            """;

        // Act + Assert
        mockMvc.perform(put("/events/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Updated meeting"))
                .andExpect(jsonPath("$.start")
                        .value("2026-08-24T11:00:00-05:00"))
                .andExpect(jsonPath("$.end")
                        .value("2026-08-24T12:00:00-05:00"));
    }

    @Test
    void shouldDeleteExistingEvent() throws Exception {

        // Arrange
        String request = """
            {
                "title": "Meeting to delete",
                "start": "2026-08-24T09:00:00-05:00",
                "end": "2026-08-24T10:00:00-05:00"
            }
            """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        var id = repository.findAll().getFirst().getId();

        // Act
        mockMvc.perform(delete("/events/{id}", id))
                .andExpect(status().isNoContent());

        // Assert
        mockMvc.perform(get("/events")
                        .param("from", "2026-08-24T09:00:00-05:00")
                        .param("to", "2026-08-24T17:00:00-05:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldRespectSaturdayClosingTime() throws Exception {

        // Arrange
        String validEvent = """
            {
                "title": "Saturday meeting",
                "start": "2026-08-22T15:00:00-05:00",
                "end": "2026-08-22T16:00:00-05:00"
            }
            """;

        // Act + Assert
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEvent))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectSundayEvent() throws Exception {

        // Arrange
        String request = """
            {
                "title": "Sunday meeting",
                "start": "2026-08-23T10:00:00-05:00",
                "end": "2026-08-23T11:00:00-05:00"
            }
            """;

        // Act + Assert
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("OUTSIDE_SCHEDULING_HOURS"));
    }
}