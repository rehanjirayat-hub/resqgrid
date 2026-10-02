package service;

import exception.InvalidStateTransitionException;
import exception.ResourceNotFoundException;
import model.Dispatch;
import model.DispatchStatus;
import model.EmergencyResource;
import model.Incident;
import model.Location;
import model.ResourceStatus;
import model.ResourceType;
import model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import repository.DispatchRepository;
import repository.IncidentRepository;
import repository.LocationRepository;
import repository.ResourceRepository;
import repository.ResponseTeamRepository;
import support.TestDataCleaner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DispatchLifecycleServiceTest {

    private final LocationRepository locationRepository = new LocationRepository();
    private final ResourceRepository resourceRepository =
            new ResourceRepository(locationRepository);
    private final ResponseTeamRepository responseTeamRepository =
            new ResponseTeamRepository();
    private final IncidentRepository incidentRepository =
            new IncidentRepository(locationRepository);
    private final DispatchRepository dispatchRepository = new DispatchRepository(
            incidentRepository,
            resourceRepository,
            responseTeamRepository
    );

    private final ResourceAssignmentService assignmentService =
            new ResourceAssignmentService(dispatchRepository, resourceRepository);

    private final DispatchLifecycleService lifecycleService =
            new DispatchLifecycleService(dispatchRepository, resourceRepository);

    @BeforeEach
    void resetDatabase() {
        TestDataCleaner.clearAll();
    }

    private long createIncident() {

        return incidentRepository.save(
                new Incident("Lifecycle incident", Severity.HIGH,
                        new Location(12.9716, 77.5946))
        );
    }

    private EmergencyResource createResource() {

        long id = resourceRepository.save(
                new EmergencyResource(0, ResourceType.AMBULANCE,
                        new Location(12.9816, 77.6046))
        );

        return resourceRepository.findById(id).orElseThrow();
    }

    private long assignResource(long incidentId, EmergencyResource resource) {

        Incident incident = incidentRepository.findById(incidentId).orElseThrow();

        AssignmentResult result =
                assignmentService.assign(incident, resource, null);

        assertTrue(result.assigned(), "Precondition: resource was assigned.");

        return result.dispatchId();
    }

    @Test
    void startShouldMoveDispatchFromCreatedToInProgress() {

        long dispatchId = assignResource(createIncident(), createResource());

        lifecycleService.start(dispatchId);

        assertEquals(
                DispatchStatus.IN_PROGRESS,
                dispatchRepository.findById(dispatchId).orElseThrow().getStatus()
        );
    }

    @Test
    void startShouldRejectAlreadyInProgressDispatch() {

        long dispatchId = assignResource(createIncident(), createResource());

        lifecycleService.start(dispatchId);

        assertThrows(
                InvalidStateTransitionException.class,
                () -> lifecycleService.start(dispatchId)
        );
    }

    @Test
    void completeShouldMarkDispatchCompleted() {

        long dispatchId = assignResource(createIncident(), createResource());

        lifecycleService.start(dispatchId);
        lifecycleService.complete(dispatchId);

        assertEquals(
                DispatchStatus.COMPLETED,
                dispatchRepository.findById(dispatchId).orElseThrow().getStatus()
        );
    }

    @Test
    void completeShouldReturnResourceToAvailable() {

        long incidentId = createIncident();
        EmergencyResource resource = createResource();

        long dispatchId = assignResource(incidentId, resource);

        lifecycleService.start(dispatchId);
        lifecycleService.complete(dispatchId);

        assertEquals(
                ResourceStatus.AVAILABLE,
                resourceRepository.findById(resource.getId())
                                  .orElseThrow()
                                  .getStatus()
        );
    }

    @Test
    void completedResourceShouldBeEligibleForDispatchAgain() {

        long incidentId = createIncident();
        EmergencyResource resource = createResource();

        long dispatchId = assignResource(incidentId, resource);

        lifecycleService.start(dispatchId);
        lifecycleService.complete(dispatchId);

        assertTrue(
                new ResourceEligibilityFilter(
                        new repository.CapabilityRepository(),
                        dispatchRepository
                ).isEligible(
                        resourceRepository.findById(resource.getId()).orElseThrow(),
                        IncidentRequirements.of(ResourceType.AMBULANCE)
                ),
                "A completed dispatch must not keep the resource unavailable."
        );
    }

    @Test
    void completeShouldRejectDispatchThatIsNotInProgress() {

        long dispatchId = assignResource(createIncident(), createResource());

        assertThrows(
                InvalidStateTransitionException.class,
                () -> lifecycleService.complete(dispatchId)
        );
    }

    @Test
    void completeShouldRejectUnknownDispatch() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> lifecycleService.complete(-1)
        );
    }

    @Test
    void completedDispatchShouldRemainStoredAsHistory() {

        long incidentId = createIncident();
        EmergencyResource resource = createResource();

        long dispatchId = assignResource(incidentId, resource);

        lifecycleService.start(dispatchId);
        lifecycleService.complete(dispatchId);

        assertEquals(
                1,
                dispatchRepository.findByIncident(incidentId).size(),
                "A completed dispatch must be preserved."
        );
    }
}