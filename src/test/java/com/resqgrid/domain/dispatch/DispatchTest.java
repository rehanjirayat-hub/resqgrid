package com.resqgrid.domain.dispatch;

import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.incident.IncidentSeverity;
import com.resqgrid.domain.incident.IncidentType;
import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceType;
import com.resqgrid.domain.team.ResponseTeam;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DispatchTest {

    private Location createLocation() {
        return new Location(
                12.9716,
                77.5946,
                "Bangalore"
        );
    }

    private Incident createIncident() {
        return new Incident(
                1L,
                "Medical Emergency",
                "Patient requires immediate assistance",
                IncidentType.MEDICAL,
                IncidentSeverity.HIGH,
                createLocation(),
                LocalDateTime.now()
        );
    }

    private EmergencyResource createResource() {
        return new EmergencyResource(
                1L,
                "Ambulance 101",
                ResourceType.AMBULANCE,
                createLocation(),
                Set.of(Capability.MEDICAL_RESPONSE)
        );
    }

    private ResponseTeam createTeam() {
        return new ResponseTeam(
                1L,
                "Medical Team Alpha",
                Set.of(Capability.MEDICAL_RESPONSE)
        );
    }

    private Dispatch createDispatch() {
        return new Dispatch(
                1L,
                createIncident()
        );
    }

    @Test
    void dispatchShouldStartAsPending() {
        Dispatch dispatch = createDispatch();

        assertEquals(
                DispatchStatus.PENDING,
                dispatch.getStatus()
        );
    }

    @Test
    void dispatchShouldStoreIdAndIncident() {
        Dispatch dispatch = createDispatch();

        assertEquals(1L, dispatch.getId());
        assertNotNull(dispatch.getIncident());
        assertEquals(
                "Medical Emergency",
                dispatch.getIncident().getTitle()
        );
    }

    @Test
    void dispatchShouldAssignResourceAndTeam() {
        Dispatch dispatch = createDispatch();

        EmergencyResource resource = createResource();
        ResponseTeam team = createTeam();

        dispatch.assign(resource, team);

        assertEquals(resource, dispatch.getResource());
        assertEquals(team, dispatch.getResponseTeam());
    }

    @Test
    void assigningDispatchShouldChangeStatusToAssigned() {
        Dispatch dispatch = createDispatch();

        dispatch.assign(
                createResource(),
                createTeam()
        );

        assertEquals(
                DispatchStatus.ASSIGNED,
                dispatch.getStatus()
        );
    }

    @Test
    void assigningDispatchShouldRecordDispatchedAt() {
        Dispatch dispatch = createDispatch();

        assertNull(dispatch.getDispatchedAt());

        dispatch.assign(
                createResource(),
                createTeam()
        );

        assertNotNull(dispatch.getDispatchedAt());
    }

    @Test
    void dispatchShouldRejectNullResource() {
        Dispatch dispatch = createDispatch();

        assertThrows(
                IllegalArgumentException.class,
                () -> dispatch.assign(null, createTeam())
        );
    }

    @Test
    void dispatchShouldRejectNullResponseTeam() {
        Dispatch dispatch = createDispatch();

        assertThrows(
                IllegalArgumentException.class,
                () -> dispatch.assign(createResource(), null)
        );
    }

    @Test
    void assignedDispatchShouldNotBeAssignedAgain() {
        Dispatch dispatch = createDispatch();

        dispatch.assign(
                createResource(),
                createTeam()
        );

        assertThrows(
                IllegalStateException.class,
                () -> dispatch.assign(
                        createResource(),
                        createTeam()
                )
        );
    }

    @Test
    void dispatchShouldMoveFromAssignedToInProgress() {
        Dispatch dispatch = createDispatch();

        dispatch.assign(
                createResource(),
                createTeam()
        );

        dispatch.changeStatus(
                DispatchStatus.IN_PROGRESS
        );

        assertEquals(
                DispatchStatus.IN_PROGRESS,
                dispatch.getStatus()
        );
    }

    @Test
    void dispatchShouldMoveFromInProgressToCompleted() {
        Dispatch dispatch = createDispatch();

        dispatch.assign(
                createResource(),
                createTeam()
        );

        dispatch.changeStatus(
                DispatchStatus.IN_PROGRESS
        );

        dispatch.changeStatus(
                DispatchStatus.COMPLETED
        );

        assertEquals(
                DispatchStatus.COMPLETED,
                dispatch.getStatus()
        );
    }

    @Test
    void pendingDispatchCanBeCancelled() {
        Dispatch dispatch = createDispatch();

        dispatch.changeStatus(
                DispatchStatus.CANCELLED
        );

        assertEquals(
                DispatchStatus.CANCELLED,
                dispatch.getStatus()
        );
    }

    @Test
    void assignedDispatchCanBeCancelled() {
        Dispatch dispatch = createDispatch();

        dispatch.assign(
                createResource(),
                createTeam()
        );

        dispatch.changeStatus(
                DispatchStatus.CANCELLED
        );

        assertEquals(
                DispatchStatus.CANCELLED,
                dispatch.getStatus()
        );
    }

    @Test
    void inProgressDispatchCanBeCancelled() {
        Dispatch dispatch = createDispatch();

        dispatch.assign(
                createResource(),
                createTeam()
        );

        dispatch.changeStatus(
                DispatchStatus.IN_PROGRESS
        );

        dispatch.changeStatus(
                DispatchStatus.CANCELLED
        );

        assertEquals(
                DispatchStatus.CANCELLED,
                dispatch.getStatus()
        );
    }

    @Test
    void completedDispatchCannotChangeStatus() {
        Dispatch dispatch = createDispatch();

        dispatch.assign(
                createResource(),
                createTeam()
        );

        dispatch.changeStatus(
                DispatchStatus.IN_PROGRESS
        );

        dispatch.changeStatus(
                DispatchStatus.COMPLETED
        );

        assertThrows(
                IllegalStateException.class,
                () -> dispatch.changeStatus(
                        DispatchStatus.CANCELLED
                )
        );
    }

    @Test
    void cancelledDispatchCannotChangeStatus() {
        Dispatch dispatch = createDispatch();

        dispatch.changeStatus(
                DispatchStatus.CANCELLED
        );

        assertThrows(
                IllegalStateException.class,
                () -> dispatch.changeStatus(
                        DispatchStatus.ASSIGNED
                )
        );
    }

    @Test
    void dispatchShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Dispatch(
                        0L,
                        createIncident()
                )
        );
    }

    @Test
    void dispatchShouldRejectNullIncident() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Dispatch(
                        1L,
                        null
                )
        );
    }

    @Test
    void dispatchShouldRejectNullStatus() {
        Dispatch dispatch = createDispatch();

        assertThrows(
                IllegalArgumentException.class,
                () -> dispatch.changeStatus(null)
        );
    }

    @Test
    void pendingDispatchCannotMoveDirectlyToCompleted() {
        Dispatch dispatch = createDispatch();

        assertThrows(
                IllegalStateException.class,
                () -> dispatch.changeStatus(
                        DispatchStatus.COMPLETED
                )
        );
    }

    @Test
    void pendingDispatchCannotMoveDirectlyToInProgress() {
        Dispatch dispatch = createDispatch();

        assertThrows(
                IllegalStateException.class,
                () -> dispatch.changeStatus(
                        DispatchStatus.IN_PROGRESS
                )
        );
    }
}