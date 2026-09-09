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
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDispatchServiceTest {

    private final ResourceDispatchService dispatchService =
            new ResourceDispatchService();

    @Test
    void shouldDispatchIncidentToNearestEligibleResource() {
        Incident incident = createAssessedMedicalIncident();

        EmergencyResource fartherResource = new EmergencyResource(
                1L,
                "Ambulance 101",
                ResourceType.AMBULANCE,
                new Location(
                        12.9716,
                        77.5946,
                        "Bangalore"
                ),
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        EmergencyResource nearestResource = new EmergencyResource(
                2L,
                "Ambulance 102",
                ResourceType.AMBULANCE,
                new Location(
                        12.2958,
                        76.6394,
                        "Mysuru"
                ),
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        ResponseTeam team = createMedicalTeam();

        Dispatch dispatch = dispatchService.dispatchIncident(
                incident,
                List.of(fartherResource, nearestResource),
                List.of(team)
        );

        assertEquals(
                nearestResource,
                dispatch.getResource()
        );

        assertEquals(
                team,
                dispatch.getResponseTeam()
        );

        assertEquals(
                DispatchStatus.ASSIGNED,
                dispatch.getStatus()
        );
    }

    @Test
    void shouldChangeIncidentStatusToDispatched() {
        Incident incident = createAssessedMedicalIncident();

        EmergencyResource resource = createAmbulance();
        ResponseTeam team = createMedicalTeam();

        dispatchService.dispatchIncident(
                incident,
                List.of(resource),
                List.of(team)
        );

        assertEquals(
                IncidentStatus.DISPATCHED,
                incident.getStatus()
        );
    }

    @Test
    void shouldMarkSelectedResourceAsBusy() {
        Incident incident = createAssessedMedicalIncident();

        EmergencyResource resource = createAmbulance();
        ResponseTeam team = createMedicalTeam();

        dispatchService.dispatchIncident(
                incident,
                List.of(resource),
                List.of(team)
        );

        assertEquals(
                ResourceStatus.BUSY,
                resource.getStatus()
        );
    }

    @Test
    void shouldMarkSelectedTeamAsDeployed() {
        Incident incident = createAssessedMedicalIncident();

        EmergencyResource resource = createAmbulance();
        ResponseTeam team = createMedicalTeam();

        dispatchService.dispatchIncident(
                incident,
                List.of(resource),
                List.of(team)
        );

        assertEquals(
                TeamStatus.DEPLOYED,
                team.getStatus()
        );
    }

    @Test
    void shouldRejectIncidentThatHasNotBeenAssessed() {
        Incident incident = createMedicalIncident();

        EmergencyResource resource = createAmbulance();
        ResponseTeam team = createMedicalTeam();

        assertThrows(
                IllegalStateException.class,
                () -> dispatchService.dispatchIncident(
                        incident,
                        List.of(resource),
                        List.of(team)
                )
        );
    }

    @Test
    void shouldRejectDispatchWhenNoEligibleResourceExists() {
        Incident incident = createAssessedMedicalIncident();

        EmergencyResource resource = new EmergencyResource(
                1L,
                "Fire Unit 101",
                ResourceType.FIRE_UNIT,
                createLocation(),
                Set.of(Capability.FIRE_RESPONSE)
        );

        ResponseTeam team = createMedicalTeam();

        assertThrows(
                IllegalStateException.class,
                () -> dispatchService.dispatchIncident(
                        incident,
                        List.of(resource),
                        List.of(team)
                )
        );
    }

    @Test
    void shouldRejectDispatchWhenNoSuitableTeamExists() {
        Incident incident = createAssessedMedicalIncident();

        EmergencyResource resource = createAmbulance();

        ResponseTeam team = new ResponseTeam(
                1L,
                "Fire Team Alpha",
                Set.of(Capability.FIRE_RESPONSE)
        );

        assertThrows(
                IllegalStateException.class,
                () -> dispatchService.dispatchIncident(
                        incident,
                        List.of(resource),
                        List.of(team)
                )
        );

        assertEquals(
                ResourceStatus.AVAILABLE,
                resource.getStatus()
        );
    }

    @Test
    void shouldIgnoreBusyResources() {
        Incident incident = createAssessedMedicalIncident();

        EmergencyResource busyResource = createAmbulance();
        busyResource.changeStatus(ResourceStatus.BUSY);

        EmergencyResource availableResource = new EmergencyResource(
                2L,
                "Ambulance 102",
                ResourceType.AMBULANCE,
                createLocation(),
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        ResponseTeam team = createMedicalTeam();

        Dispatch dispatch = dispatchService.dispatchIncident(
                incident,
                List.of(busyResource, availableResource),
                List.of(team)
        );

        assertEquals(
                availableResource,
                dispatch.getResource()
        );
    }

    @Test
    void shouldIgnoreUnavailableTeams() {
        Incident incident = createAssessedMedicalIncident();

        EmergencyResource resource = createAmbulance();

        ResponseTeam unavailableTeam = createMedicalTeam();
        unavailableTeam.changeStatus(TeamStatus.DEPLOYED);

        ResponseTeam availableTeam = new ResponseTeam(
                2L,
                "Medical Team Bravo",
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        Dispatch dispatch = dispatchService.dispatchIncident(
                incident,
                List.of(resource),
                List.of(unavailableTeam, availableTeam)
        );

        assertEquals(
                availableTeam,
                dispatch.getResponseTeam()
        );
    }

    @Test
    void shouldRejectNullIncident() {
        assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.dispatchIncident(
                        null,
                        List.of(createAmbulance()),
                        List.of(createMedicalTeam())
                )
        );
    }

    @Test
    void shouldRejectNullResources() {
        Incident incident = createAssessedMedicalIncident();

        assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.dispatchIncident(
                        incident,
                        null,
                        List.of(createMedicalTeam())
                )
        );
    }

    @Test
    void shouldRejectNullResponseTeams() {
        Incident incident = createAssessedMedicalIncident();

        assertThrows(
                IllegalArgumentException.class,
                () -> dispatchService.dispatchIncident(
                        incident,
                        List.of(createAmbulance()),
                        null
                )
        );
    }

    private Incident createMedicalIncident() {
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

    private Incident createAssessedMedicalIncident() {
        Incident incident = createMedicalIncident();

        incident.changeStatus(IncidentStatus.ASSESSED);

        return incident;
    }

    private EmergencyResource createAmbulance() {
        return new EmergencyResource(
                1L,
                "Ambulance 101",
                ResourceType.AMBULANCE,
                createLocation(),
                Set.of(Capability.MEDICAL_RESPONSE)
        );
    }

    private ResponseTeam createMedicalTeam() {
        return new ResponseTeam(
                1L,
                "Medical Team Alpha",
                Set.of(Capability.MEDICAL_RESPONSE)
        );
    }

    private Location createLocation() {
        return new Location(
                12.2958,
                76.6394,
                "Mysuru, Karnataka"
        );
    }
}
