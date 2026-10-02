package service;

import model.EmergencyResource;
import model.Incident;
import model.Location;
import model.ResourceCapability;
import model.ResourceStatus;
import model.ResourceType;
import model.Severity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import repository.CapabilityRepository;
import repository.DispatchRepository;
import repository.IncidentRepository;
import repository.LocationRepository;
import repository.ResourceRepository;
import repository.ResponseTeamRepository;
import support.TestDataCleaner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DispatchEngineTest {

    private static final LocationRepository locationRepository =
            new LocationRepository();
    private static final CapabilityRepository capabilityRepository =
            new CapabilityRepository();
    private static final ResourceRepository resourceRepository =
            new ResourceRepository(locationRepository);
    private static final ResponseTeamRepository responseTeamRepository =
            new ResponseTeamRepository();
    private static final IncidentRepository incidentRepository =
            new IncidentRepository(locationRepository);
    private static final DispatchRepository dispatchRepository = new DispatchRepository(
            incidentRepository,
            resourceRepository,
            responseTeamRepository
    );

    private static ResourceAssignmentService assignmentService;
    private static DispatchEngine engine;

    @BeforeAll
    static void setUpEngine() {

        assignmentService = new ResourceAssignmentService(
                dispatchRepository,
                resourceRepository
        );

        engine = new DispatchEngine(
                resourceRepository,
                new ResourceEligibilityFilter(
                        capabilityRepository,
                        dispatchRepository
                ),
                new ResourceRankingService(dispatchRepository),
                assignmentService
        );
    }

    @BeforeEach
    void resetDatabase() {

        TestDataCleaner.clearAll();
    }

    private long createIncident(String description, double latitude, double longitude) {

        return incidentRepository.save(
                new Incident(
                        description,
                        Severity.CRITICAL,
                        new Location(latitude, longitude)
                )
        );
    }

    private EmergencyResource createResource(ResourceType type,
                                             double latitude,
                                             double longitude) {

        long id = resourceRepository.save(
                new EmergencyResource(0, type, new Location(latitude, longitude))
        );

        return resourceRepository.findById(id).orElseThrow();
    }

    @Test
    void engineShouldSelectNearestEligibleResourceNotTheFirstCreated() {

        Incident incident = incidentRepository.findById(
                createIncident("Ranking incident", 12.9716, 77.5946)
        ).orElseThrow();

        // The far resource is created first, so a first-available engine
        // would select it.
        EmergencyResource far = createResource(
                ResourceType.AMBULANCE,
                19.0760,
                72.8777
        );

        EmergencyResource near = createResource(
                ResourceType.AMBULANCE,
                12.9816,
                77.6046
        );

        assertTrue(far.getId() < near.getId(), "Precondition: far was created first.");

        DispatchOutcome outcome = engine.dispatch(
                incident,
                IncidentRequirements.of(ResourceType.AMBULANCE),
                null
        );

        assertTrue(outcome.isAssigned());
        assertEquals(
                near.getId(),
                outcome.selectedCandidate().getResource().getId(),
                "The nearer resource must be selected."
        );
    }

    @Test
    void engineShouldPreferLowerWorkloadWhenDistancesAreEqual() {

        Incident incident = incidentRepository.findById(
                createIncident("Workload incident", 12.9716, 77.5946)
        ).orElseThrow();

        // Both resources sit at the same coordinates, so distance is equal and
        // the deciding factor is workload.
        EmergencyResource busy = createResource(
                ResourceType.AMBULANCE,
                12.9716,
                77.5946
        );

        EmergencyResource idle = createResource(
                ResourceType.AMBULANCE,
                12.9716,
                77.5946
        );

        Incident previousIncident = incidentRepository.findById(
                createIncident("Previous incident", 12.9716, 77.5946)
        ).orElseThrow();

        dispatchRepository.save(
                new model.Dispatch(0, previousIncident, busy, null)
        );

        resourceRepository.updateStatus(busy.getId(), ResourceStatus.AVAILABLE);

        DispatchOutcome outcome = engine.dispatch(
                incident,
                IncidentRequirements.of(ResourceType.AMBULANCE),
                null
        );

        assertTrue(outcome.isAssigned());
        assertEquals(
                idle.getId(),
                outcome.selectedCandidate().getResource().getId(),
                "The resource with no history must outrank the one with a dispatch."
        );
    }

    @Test
    void engineShouldRejectResourceMissingRequiredCapability() {

        Incident incident = incidentRepository.findById(
                createIncident("Capability incident", 12.9716, 77.5946)
        ).orElseThrow();

        EmergencyResource resource = createResource(
                ResourceType.AMBULANCE,
                12.9716,
                77.5946
        );

        DispatchOutcome outcome = engine.dispatch(
                incident,
                IncidentRequirements.of(ResourceType.AMBULANCE)
                                   .requiring(ResourceCapability.MEDICAL_RESPONSE),
                null
        );

        assertEquals(
                DispatchOutcome.Status.NO_ELIGIBLE_RESOURCE,
                outcome.status(),
                "A resource without the required capability must be rejected."
        );
        assertFalse(outcome.isAssigned());
        assertTrue(dispatchRepository.findByResource(resource.getId()).isEmpty());
    }

    @Test
    void engineShouldRejectResourceOfWrongType() {

        Incident incident = incidentRepository.findById(
                createIncident("Type incident", 12.9716, 77.5946)
        ).orElseThrow();

        createResource(ResourceType.FIRE_UNIT, 12.9716, 77.5946);

        DispatchOutcome outcome = engine.dispatch(
                incident,
                IncidentRequirements.of(ResourceType.AMBULANCE),
                null
        );

        assertEquals(DispatchOutcome.Status.NO_ELIGIBLE_RESOURCE, outcome.status());
    }

    @Test
    void engineShouldNotAssignResourceThatIsAlreadyBusy() {

        Incident incident = incidentRepository.findById(
                createIncident("Busy incident", 12.9716, 77.5946)
        ).orElseThrow();

        EmergencyResource resource = createResource(
                ResourceType.AMBULANCE,
                12.9716,
                77.5946
        );

        resourceRepository.updateStatus(resource.getId(), ResourceStatus.BUSY);

        DispatchOutcome outcome = engine.dispatch(
                incident,
                IncidentRequirements.of(ResourceType.AMBULANCE),
                null
        );

        assertEquals(DispatchOutcome.Status.NO_ELIGIBLE_RESOURCE, outcome.status());
    }

    @Test
    void engineShouldMarkAssignedResourceBusy() {

        Incident incident = incidentRepository.findById(
                createIncident("State incident", 22.5726, 88.3639)
        ).orElseThrow();

        EmergencyResource resource = createResource(
                ResourceType.AMBULANCE,
                22.5726,
                88.3639
        );

        DispatchOutcome outcome = engine.dispatch(
                incident,
                IncidentRequirements.of(ResourceType.AMBULANCE),
                null
        );

        assertTrue(outcome.isAssigned());
        assertEquals(
                ResourceStatus.BUSY,
                resourceRepository.findById(resource.getId()).orElseThrow().getStatus()
        );
        assertEquals(1, dispatchRepository.findByResource(resource.getId()).size());
    }

    @Test
    void engineShouldRankRemainingCandidatesWhenFirstBecomesUnavailable() {

        Incident incident = incidentRepository.findById(
                createIncident("Retry incident", 24.8607, 67.0011)
        ).orElseThrow();

        EmergencyResource first = createResource(
                ResourceType.AMBULANCE,
                24.8607,
                67.0011
        );

        EmergencyResource second = createResource(
                ResourceType.AMBULANCE,
                25.0000,
                67.5000
        );

        // Another operation claims the better candidate after it was ranked
        // but before this operation assigns it.
        resourceRepository.updateStatus(first.getId(), ResourceStatus.BUSY);

        DispatchOutcome outcome = engine.dispatch(
                incident,
                IncidentRequirements.of(ResourceType.AMBULANCE),
                null
        );

        assertTrue(outcome.isAssigned());
        assertEquals(
                second.getId(),
                outcome.selectedCandidate().getResource().getId(),
                "The engine must fall back to the next eligible candidate."
        );
    }

    @Test
    void rankingShouldBeDeterministicForIdenticalCandidates() {

        Incident incident = incidentRepository.findById(
                createIncident("Determinism incident", 13.0827, 80.2707)
        ).orElseThrow();

        EmergencyResource first = createResource(
                ResourceType.FIRE_UNIT,
                13.0827,
                80.2707
        );

        EmergencyResource second = createResource(
                ResourceType.FIRE_UNIT,
                13.0827,
                80.2707
        );

        ResourceRankingService rankingService =
                new ResourceRankingService(dispatchRepository);

        List<RankedCandidate> firstOrder =
                rankingService.rank(incident, List.of(first, second));

        List<RankedCandidate> reversedOrder =
                rankingService.rank(incident, List.of(second, first));

        assertEquals(
                firstOrder.get(0).getResource().getId(),
                reversedOrder.get(0).getResource().getId(),
                "Tie-breaking must not depend on input order."
        );
        assertEquals(first.getId(), firstOrder.get(0).getResource().getId());
    }
}