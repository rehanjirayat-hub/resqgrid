package com.resqgrid.service.dispatch;

import com.resqgrid.domain.dispatch.Dispatch;
import com.resqgrid.domain.dispatch.DispatchStatus;
import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.incident.IncidentSeverity;
import com.resqgrid.domain.incident.IncidentStatus;
import com.resqgrid.domain.incident.IncidentType;
import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.resource.ResourceType;
import com.resqgrid.domain.team.ResponseTeam;
import com.resqgrid.domain.team.TeamStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DispatchLifecycleServiceTest {

    private DispatchLifecycleService lifecycleService;

    @BeforeEach
    void setUp() {
        lifecycleService = new DispatchLifecycleService();
    }

    @Test
    void startDispatch_shouldChangeDispatchToInProgress() {

        Dispatch dispatch = createAssignedDispatch();

        lifecycleService.startDispatch(dispatch);

        assertEquals(
                DispatchStatus.IN_PROGRESS,
                dispatch.getStatus()
        );
    }

    @Test
    void startDispatch_shouldChangeIncidentToInProgress() {

        Dispatch dispatch = createAssignedDispatch();

        lifecycleService.startDispatch(dispatch);

        assertEquals(
                IncidentStatus.IN_PROGRESS,
                dispatch.getIncident().getStatus()
        );
    }

    @Test
    void completeDispatch_shouldChangeDispatchToCompleted() {

        Dispatch dispatch = createInProgressDispatch();

        lifecycleService.completeDispatch(dispatch);

        assertEquals(
                DispatchStatus.COMPLETED,
                dispatch.getStatus()
        );
    }

    @Test
    void completeDispatch_shouldMakeResourceAvailable() {

        Dispatch dispatch = createInProgressDispatch();

        lifecycleService.completeDispatch(dispatch);

        assertEquals(
                ResourceStatus.AVAILABLE,
                dispatch.getResource().getStatus()
        );
    }

    @Test
    void completeDispatch_shouldMakeResponseTeamAvailable() {

        Dispatch dispatch = createInProgressDispatch();

        lifecycleService.completeDispatch(dispatch);

        assertEquals(
                TeamStatus.AVAILABLE,
                dispatch.getResponseTeam().getStatus()
        );
    }

    @Test
    void completeDispatch_shouldResolveIncident() {

        Dispatch dispatch = createInProgressDispatch();

        lifecycleService.completeDispatch(dispatch);

        assertEquals(
                IncidentStatus.RESOLVED,
                dispatch.getIncident().getStatus()
        );
    }

    @Test
    void startDispatch_shouldRejectNullDispatch() {

        assertThrows(
                IllegalArgumentException.class,
                () -> lifecycleService.startDispatch(null)
        );
    }

    @Test
    void completeDispatch_shouldRejectNullDispatch() {

        assertThrows(
                IllegalArgumentException.class,
                () -> lifecycleService.completeDispatch(null)
        );
    }

    @Test
    void completeDispatch_shouldRejectAlreadyCompletedDispatch() {

        Dispatch dispatch = createInProgressDispatch();

        lifecycleService.completeDispatch(dispatch);

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.completeDispatch(dispatch)
        );
    }

    @Test
    void completeDispatch_shouldRejectCancelledDispatch() {

        Dispatch dispatch = createAssignedDispatch();

        dispatch.changeStatus(DispatchStatus.CANCELLED);

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.completeDispatch(dispatch)
        );
    }

    private Dispatch createAssignedDispatch() {

        Incident incident = createAssessedIncident();

        EmergencyResource resource = createBusyResource();

        ResponseTeam responseTeam = createDeployedTeam();

        Dispatch dispatch = new Dispatch(
                1L,
                incident
        );

        dispatch.assign(
                resource,
                responseTeam
        );

        incident.changeStatus(IncidentStatus.DISPATCHED);

        return dispatch;
    }

    private Dispatch createInProgressDispatch() {

        Dispatch dispatch = createAssignedDispatch();

        dispatch.changeStatus(DispatchStatus.IN_PROGRESS);

        dispatch.getIncident().changeStatus(
                IncidentStatus.IN_PROGRESS
        );

        return dispatch;
    }

    private Incident createAssessedIncident() {

        Incident incident = new Incident(
                1L,
                "Medical Emergency",
                "Patient requires immediate assistance",
                IncidentType.MEDICAL,
                IncidentSeverity.HIGH,
                createLocation(),
                LocalDateTime.now()
        );

        incident.changeStatus(IncidentStatus.ASSESSED);

        return incident;
    }

    private EmergencyResource createBusyResource() {

        EmergencyResource resource = new EmergencyResource(
                1L,
                "Ambulance 101",
                ResourceType.AMBULANCE,
                createLocation(),
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        resource.changeStatus(ResourceStatus.BUSY);

        return resource;
    }

    private ResponseTeam createDeployedTeam() {

        ResponseTeam responseTeam = new ResponseTeam(
                1L,
                "Medical Team Alpha",
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        responseTeam.changeStatus(TeamStatus.DEPLOYED);

        return responseTeam;
    }

    private Location createLocation() {

        return new Location(
                12.2958,
                76.6394,
                "Mysuru, Karnataka"
        );
    }
}
