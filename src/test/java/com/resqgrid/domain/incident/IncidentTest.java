package com.resqgrid.domain.incident;

import com.resqgrid.domain.location.Location;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IncidentTest {

    @Test
    void newIncidentShouldStartWithReportedStatus() {
        Incident incident = createValidIncident();

        assertEquals(IncidentStatus.REPORTED, incident.getStatus());
    }

    @Test
    void incidentShouldFollowValidStatusProgression() {
        Incident incident = createValidIncident();

        incident.changeStatus(IncidentStatus.ASSESSED);
        incident.changeStatus(IncidentStatus.DISPATCHED);
        incident.changeStatus(IncidentStatus.IN_PROGRESS);
        incident.changeStatus(IncidentStatus.RESOLVED);

        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
    }

    @Test
    void invalidStatusTransitionShouldThrowException() {
        Incident incident = createValidIncident();

        assertThrows(
                IllegalStateException.class,
                () -> incident.changeStatus(IncidentStatus.RESOLVED)
        );
    }

    @Test
    void resolvedIncidentShouldNotChangeStatus() {
        Incident incident = createValidIncident();

        incident.changeStatus(IncidentStatus.ASSESSED);
        incident.changeStatus(IncidentStatus.DISPATCHED);
        incident.changeStatus(IncidentStatus.IN_PROGRESS);
        incident.changeStatus(IncidentStatus.RESOLVED);

        assertThrows(
                IllegalStateException.class,
                () -> incident.changeStatus(IncidentStatus.REPORTED)
        );
    }

    @Test
    void blankTitleShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Incident(
                        1,
                        "",
                        "Building fire",
                        IncidentSeverity.CRITICAL,
                        createValidLocation(),
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void nullSeverityShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Incident(
                        1,
                        "Building Fire",
                        "Fire reported in a building",
                        null,
                        createValidLocation(),
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void nullLocationShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Incident(
                        1,
                        "Building Fire",
                        "Fire reported in a building",
                        IncidentSeverity.CRITICAL,
                        null,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void nullReportedAtShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Incident(
                        1,
                        "Building Fire",
                        "Fire reported in a building",
                        IncidentSeverity.CRITICAL,
                        createValidLocation(),
                        null
                )
        );
    }

    private Incident createValidIncident() {
        return new Incident(
                1,
                "Building Fire",
                "Fire reported in a commercial building",
                IncidentSeverity.CRITICAL,
                createValidLocation(),
                LocalDateTime.now()
        );
    }

    private Location createValidLocation() {
        return new Location(
                12.2958,
                76.6394,
                "Mysuru, Karnataka"
        );
    }
}
