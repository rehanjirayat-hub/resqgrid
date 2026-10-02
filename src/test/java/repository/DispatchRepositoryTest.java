package repository;

import model.Dispatch;
import model.DispatchStatus;
import model.EmergencyResource;
import model.Incident;
import model.Location;
import model.ResourceType;
import model.ResponseTeam;
import model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DispatchRepositoryTest {

    private final LocationRepository locationRepository = new LocationRepository();
    private final ResourceRepository resourceRepository =
            new ResourceRepository(locationRepository);
    private final ResponseTeamRepository responseTeamRepository =
            new ResponseTeamRepository();
    private final IncidentRepository incidentRepository =
            new IncidentRepository(locationRepository);
    private final DispatchRepository repository = new DispatchRepository(
            incidentRepository,
            resourceRepository,
            responseTeamRepository
    );

    private long incidentId;
    private long resourceId;

    @BeforeEach
    void createIncidentAndResource() {

        incidentId = incidentRepository.save(new Incident(
                "Structure fire",
                Severity.HIGH,
                new Location(12.9716, 77.5946)
        ));

        resourceId = resourceRepository.save(new EmergencyResource(
                0,
                ResourceType.FIRE_UNIT,
                new Location(13.0827, 80.2707)
        ));
    }

    private Dispatch newDispatch(ResponseTeam responseTeam) {

        Incident incident = incidentRepository.findById(incidentId).orElseThrow();
        EmergencyResource resource =
                resourceRepository.findById(resourceId).orElseThrow();

        return new Dispatch(0, incident, resource, responseTeam);
    }

    @Test
    void saveShouldInsertDispatchAndReturnGeneratedId() {

        long id = repository.save(newDispatch(null));

        assertTrue(id > 0);
    }

    @Test
    void findByIdShouldReturnDispatchWithReferences() {

        long id = repository.save(newDispatch(null));

        Optional<Dispatch> result = repository.findById(id);

        assertTrue(result.isPresent());

        Dispatch dispatch = result.get();

        assertEquals(id, dispatch.getId());
        assertEquals(incidentId, dispatch.getIncident().getId());
        assertEquals(resourceId, dispatch.getResource().getId());
        assertEquals(DispatchStatus.CREATED, dispatch.getStatus());
        assertNull(dispatch.getResponseTeam());
    }

    @Test
    void findByIdShouldReturnAssociatedResponseTeam() {

        long teamId = responseTeamRepository.save(new ResponseTeam(0));
        ResponseTeam team = responseTeamRepository.findById(teamId).orElseThrow();

        long id = repository.save(newDispatch(team));

        Dispatch dispatch = repository.findById(id).orElseThrow();

        assertEquals(teamId, dispatch.getResponseTeam().getId());
    }

    @Test
    void findByIdShouldReturnEmptyForUnknownId() {

        assertTrue(repository.findById(-1).isEmpty());
    }

    @Test
    void findByIncidentShouldReturnDispatchesForThatIncident() {

        long id = repository.save(newDispatch(null));

        List<Dispatch> result = repository.findByIncident(incidentId);

        assertTrue(result.stream().anyMatch(dispatch -> dispatch.getId() == id));
    }

    @Test
    void findByResourceShouldReturnDispatchesForThatResource() {

        long id = repository.save(newDispatch(null));

        List<Dispatch> result = repository.findByResource(resourceId);

        assertTrue(result.stream().anyMatch(dispatch -> dispatch.getId() == id));
    }

    @Test
    void updateStatusShouldChangeStoredStatus() {

        long id = repository.save(newDispatch(null));

        repository.updateStatus(id, DispatchStatus.IN_PROGRESS);

        Dispatch updated = repository.findById(id).orElseThrow();

        assertEquals(DispatchStatus.IN_PROGRESS, updated.getStatus());
        assertTrue(repository.findByStatus(DispatchStatus.IN_PROGRESS)
                               .stream()
                               .anyMatch(dispatch -> dispatch.getId() == id));
    }

    @Test
    void saveShouldRejectUnknownIncident() {

        Incident unknownIncident = new Incident(
                -1,
                "Missing incident",
                Severity.LOW,
                model.Status.REPORTED,
                new Location(1.0, 1.0),
                java.time.LocalDateTime.now()
        );

        EmergencyResource resource =
                resourceRepository.findById(resourceId).orElseThrow();

        Dispatch dispatch = new Dispatch(0, unknownIncident, resource, null);

        assertThrows(RuntimeException.class, () -> repository.save(dispatch));
    }

    @Test
    void updateStatusShouldRejectUnknownDispatch() {

        assertThrows(
                RuntimeException.class,
                () -> repository.updateStatus(-1, DispatchStatus.COMPLETED)
        );
    }
}
