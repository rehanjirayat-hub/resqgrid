package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DispatchTest {

    @Test
    void newDispatchShouldStartAsCreated() {
        Incident incident = new Incident(
                "Medical emergency",
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        assertEquals(DispatchStatus.CREATED, dispatch.getStatus());
    }

    @Test
    void dispatchShouldStoreItsId() {
        Incident incident = new Incident(
                "Medical emergency",
                Severity.HIGH,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                10,
                incident,
                resource,
                responseTeam
        );

        assertEquals(10, dispatch.getId());
    }

    @Test
    void dispatchShouldStoreIncident() {
        Incident incident = new Incident(
                "Fire emergency",
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.FIRE_UNIT,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        assertEquals(incident, dispatch.getIncident());
    }

    @Test
    void dispatchShouldStoreResource() {
        Incident incident = new Incident(
                "Medical emergency",
                Severity.HIGH,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                5,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        assertEquals(resource, dispatch.getResource());
    }

    @Test
    void dispatchShouldStoreResponseTeam() {
        Incident incident = new Incident(
                "Rescue emergency",
                Severity.MEDIUM,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                5,
                ResourceType.RESCUE_TEAM,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(7);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        assertEquals(responseTeam, dispatch.getResponseTeam());
    }

    @Test
    void dispatchShouldSetCreationTime() {
        Incident incident = new Incident(
                "Medical emergency",
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        assertNotNull(dispatch.getCreatedAt());
    }

    @Test
    void dispatchShouldMoveFromCreatedToInProgress() {
        Incident incident = new Incident(
                "Medical emergency",
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        dispatch.transitionTo(DispatchStatus.IN_PROGRESS);

        assertEquals(DispatchStatus.IN_PROGRESS, dispatch.getStatus());
    }

    @Test
    void dispatchShouldMoveFromInProgressToCompleted() {
        Incident incident = new Incident(
                "Medical emergency",
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        dispatch.transitionTo(DispatchStatus.IN_PROGRESS);
        dispatch.transitionTo(DispatchStatus.COMPLETED);

        assertEquals(DispatchStatus.COMPLETED, dispatch.getStatus());
    }

    @Test
    void dispatchShouldNotAllowInvalidTransition() {
        Incident incident = new Incident(
                "Medical emergency",
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        dispatch.transitionTo(DispatchStatus.COMPLETED);

        assertEquals(DispatchStatus.CREATED, dispatch.getStatus());
    }

    @Test
    void completedDispatchShouldNotChangeState() {
        Incident incident = new Incident(
                "Medical emergency",
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        dispatch.transitionTo(DispatchStatus.IN_PROGRESS);
        dispatch.transitionTo(DispatchStatus.COMPLETED);
        dispatch.transitionTo(DispatchStatus.IN_PROGRESS);

        assertEquals(DispatchStatus.COMPLETED, dispatch.getStatus());
    }
}