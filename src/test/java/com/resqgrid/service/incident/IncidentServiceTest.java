package com.resqgrid.service.incident;

import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.incident.IncidentSeverity;
import com.resqgrid.domain.incident.IncidentStatus;
import com.resqgrid.domain.location.Location;
import com.resqgrid.exception.IncidentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.resqgrid.service.incident.IncidentService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IncidentServiceTest {

    private IncidentService incidentService;
    private Location location;
    private LocalDateTime reportedAt;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService();

        location = new Location(
                12.9716,
                77.5946,
                "Bangalore"
        );

        reportedAt = LocalDateTime.now();
    }

    @Test
    void shouldReportIncidentSuccessfully() {
        Incident incident = incidentService.reportIncident(
                1L,
                "Building Fire",
                "Fire reported in a residential building",
                IncidentSeverity.CRITICAL,
                location,
                reportedAt
        );

        assertNotNull(incident);
        assertEquals(1L, incident.getId());
        assertEquals("Building Fire", incident.getTitle());
        assertEquals(
                IncidentSeverity.CRITICAL,
                incident.getSeverity()
        );
    }

    @Test
    void shouldRejectDuplicateIncidentId() {
        incidentService.reportIncident(
                1L,
                "First Incident",
                "First incident description",
                IncidentSeverity.HIGH,
                location,
                reportedAt
        );

        assertThrows(
                IllegalStateException.class,
                () -> incidentService.reportIncident(
                        1L,
                        "Second Incident",
                        "Second incident description",
                        IncidentSeverity.LOW,
                        location,
                        reportedAt
                )
        );
    }

    @Test
    void shouldPreserveIncidentInitialStatus() {
        Incident incident = incidentService.reportIncident(
                1L,
                "Medical Emergency",
                "Patient requires immediate assistance",
                IncidentSeverity.HIGH,
                location,
                reportedAt
        );

        assertEquals(
                IncidentStatus.REPORTED,
                incident.getStatus()
        );
    }

    @Test
    void shouldRejectInvalidIncidentData() {
        assertThrows(
                IllegalArgumentException.class,
                () -> incidentService.reportIncident(
                        0L,
                        "Invalid Incident",
                        "Invalid incident description",
                        IncidentSeverity.MEDIUM,
                        location,
                        reportedAt
                )
        );
    }

    @Test
    void shouldFindExistingIncidentById() {
        Incident reportedIncident = incidentService.reportIncident(
                1L,
                "Building Fire",
                "Fire reported in a residential building",
                IncidentSeverity.CRITICAL,
                location,
                reportedAt
        );

        Incident foundIncident =
                incidentService.findIncidentById(1L);

        assertEquals(reportedIncident, foundIncident);
    }

    @Test
    void shouldThrowExceptionWhenIncidentDoesNotExist() {
        assertThrows(
                IncidentNotFoundException.class,
                () -> incidentService.findIncidentById(999L)
        );
    }

    @Test
    void shouldChangeIncidentStatusSuccessfully() {
        incidentService.reportIncident(
                1L,
                "Building Fire",
                "Fire reported in a residential building",
                IncidentSeverity.CRITICAL,
                location,
                reportedAt
        );

        incidentService.changeIncidentStatus(
                1L,
                IncidentStatus.ASSESSED
        );

        Incident incident = incidentService.findIncidentById(1L);

        assertEquals(
                IncidentStatus.ASSESSED,
                incident.getStatus()
        );
    }

    @Test
    void shouldRejectInvalidIncidentStatusTransition() {
        incidentService.reportIncident(
                1L,
                "Building Fire",
                "Fire reported in a residential building",
                IncidentSeverity.CRITICAL,
                location,
                reportedAt
        );

        assertThrows(
                IllegalStateException.class,
                () -> incidentService.changeIncidentStatus(
                        1L,
                        IncidentStatus.RESOLVED
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenChangingStatusOfMissingIncident() {
        assertThrows(
                IncidentNotFoundException.class,
                () -> incidentService.changeIncidentStatus(
                        999L,
                        IncidentStatus.ASSESSED
                )
        );
    }

    @Test
    void shouldRejectNullIncidentStatus() {
        incidentService.reportIncident(
                1L,
                "Building Fire",
                "Fire reported in a residential building",
                IncidentSeverity.CRITICAL,
                location,
                reportedAt
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> incidentService.changeIncidentStatus(
                        1L,
                        null
                )
        );
    }


}