package model;

import org.junit.jupiter.api.Test;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IncidentTest {

    @Test
    void newIncidentShouldStartAsReported() {
        Incident incident = new Incident(
                "Fire emergency",
                Severity.CRITICAL,
                new Location(12,21)
        );

        assertEquals(Status.REPORTED, incident.getStatus());
    }

    @Test
    void incidentShouldTransitionFromReportedToAssessed(){
        Incident incident = new Incident(
                "Fire emergency",
                Severity.CRITICAL,
                new Location(12,21)
        );

        incident.transitionTo(Status.ASSESSED);

        assertEquals(Status.ASSESSED, incident.getStatus());
    }

    @Test
    void incidentShouldTransitionFromAssessedToRequirementsDetermined() {
        Incident incident = new Incident(
                "Fire emergency",
                Severity.CRITICAL,
                new Location(21,12)
        );

        incident.transitionTo(Status.ASSESSED);
        incident.transitionTo(Status.REQUIREMENTS_DETERMINED);

        assertEquals(Status.REQUIREMENTS_DETERMINED, incident.getStatus());
    }

    @Test
    void incidentShouldTransitionFromRequirementsDeterminedToResponseInProgress() {
        Incident incident = new Incident(
                "Fire emergency",
                Severity.CRITICAL,
                new Location(12,21)
        );

        incident.transitionTo(Status.ASSESSED);
        incident.transitionTo(Status.REQUIREMENTS_DETERMINED);
        incident.transitionTo(Status.RESPONSE_IN_PROGRESS);

        assertEquals(Status.RESPONSE_IN_PROGRESS, incident.getStatus());
    }

    @Test
    void incidentShouldTransitionFromResponseInProgressToResolved() {
        Incident incident = new Incident(
                "Fire emergency",
                Severity.CRITICAL,
                new Location(12,21)
        );

        incident.transitionTo(Status.ASSESSED);
        incident.transitionTo(Status.REQUIREMENTS_DETERMINED);
        incident.transitionTo(Status.RESPONSE_IN_PROGRESS);
        incident.transitionTo(Status.RESOLVED);

        assertEquals(Status.RESOLVED, incident.getStatus());
    }

    @Test
    void incidentShouldNotSkipLifecycleState() {
        Incident incident = new Incident(
                "Fire emergency",
                Severity.CRITICAL,
                new Location(21,12)
        );

        incident.transitionTo(Status.RESOLVED);

        assertEquals(Status.REPORTED, incident.getStatus());
    }

    @Test
    void incidentShouldStoreBasicInformation() {
        Location location = new Location(21,21);

        Incident incident = new Incident(
                "Fire emergency",
                Severity.CRITICAL,
                location
        );

        assertEquals("Fire emergency", incident.getDescription());
        assertEquals(Severity.CRITICAL, incident.getSeverity());
        assertEquals(location, incident.getLocation());
        assertEquals(Status.REPORTED, incident.getStatus());
    }
}
