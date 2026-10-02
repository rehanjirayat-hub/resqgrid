package service;

import model.EmergencyResource;
import model.Incident;
import model.Location;
import model.ResourceStatus;
import model.ResourceType;
import model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import repository.CapabilityRepository;
import repository.DispatchRepository;
import repository.IncidentRepository;
import repository.LocationRepository;
import repository.ResourceRepository;
import repository.ResponseTeamRepository;
import support.TestDataCleaner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Walks the complete documented response flow, from a reported incident to a
 * resolved incident, and checks the resource state at each documented step.
 */
public class DispatchResponseFlowTest {

    private final LocationRepository locationRepository = new LocationRepository();
    private final CapabilityRepository capabilityRepository =
            new CapabilityRepository();
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

    private final DispatchLifecycleService dispatchLifecycleService =
            new DispatchLifecycleService(dispatchRepository, resourceRepository);

    private final IncidentLifecycleService incidentLifecycleService =
            new IncidentLifecycleService(incidentRepository);

    private DispatchEngine engine;

    @BeforeEach
    void setUp() {

        TestDataCleaner.clearAll();

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

    @Test
    void incidentShouldProgressFromReportToResolution() {

        long incidentId = incidentRepository.save(
                new Incident("Building collapse", Severity.CRITICAL,
                        new Location(12.9716, 77.5946))
        );

        long resourceId = resourceRepository.save(
                new EmergencyResource(0, ResourceType.RESCUE_TEAM,
                        new Location(12.9816, 77.6046))
        );

        Incident incident =
                incidentRepository.findById(incidentId).orElseThrow();

        incidentLifecycleService.assess(incidentId);
        incidentLifecycleService.determineRequirements(incidentId);

        DispatchOutcome outcome = engine.dispatch(
                incident,
                IncidentRequirements.of(ResourceType.RESCUE_TEAM),
                null
        );

        assertTrue(outcome.isAssigned(), "A resource should have been dispatched.");

        assertEquals(
                ResourceStatus.BUSY,
                resourceRepository.findById(resourceId).orElseThrow().getStatus(),
                "The assigned resource must become busy."
        );

        dispatchLifecycleService.start(outcome.dispatchId());

        incidentLifecycleService.startResponse(incidentId);

        dispatchLifecycleService.complete(outcome.dispatchId());

        assertEquals(
                ResourceStatus.AVAILABLE,
                resourceRepository.findById(resourceId).orElseThrow().getStatus(),
                "Completion must release the resource."
        );

        incidentLifecycleService.resolve(incidentId);

        assertEquals(
                model.Status.RESOLVED,
                incidentRepository.findById(incidentId).orElseThrow().getStatus()
        );

        assertEquals(
                1,
                dispatchRepository.findByIncident(incidentId).size(),
                "The completed dispatch must be kept as history."
        );
    }

    @Test
    void concurrentDispatchRequestsShouldNotDoubleAssignASingleResource() throws Exception {

        long resourceId = resourceRepository.save(
                new EmergencyResource(0, ResourceType.AMBULANCE,
                        new Location(12.9716, 77.5946))
        );

        int requests = 6;

        long[] incidentIds = new long[requests];

        for (int request = 0; request < requests; request++) {

            incidentIds[request] = incidentRepository.save(
                    new Incident("Concurrent incident " + request,
                            Severity.CRITICAL,
                            new Location(12.9716, 77.5946))
            );
        }

        CountDownLatch ready = new CountDownLatch(requests);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger assigned = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(requests);

        try {

            for (int request = 0; request < requests; request++) {

                long incidentId = incidentIds[request];

                executor.submit(() -> {

                    ready.countDown();
                    start.await();

                    Incident incident =
                            incidentRepository.findById(incidentId).orElseThrow();

                    DispatchOutcome outcome = engine.dispatch(
                            incident,
                            IncidentRequirements.of(ResourceType.AMBULANCE),
                            null
                    );

                    if (outcome.isAssigned()) {
                        assigned.incrementAndGet();
                    }

                    return null;
                });
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS));

            start.countDown();

            executor.shutdown();

            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        } finally {
            executor.shutdownNow();
        }

        assertEquals(
                1,
                assigned.get(),
                "Exactly one concurrent request may assign the single available resource."
        );

        assertEquals(
                1,
                dispatchRepository.findByResource(resourceId).size(),
                "Only one active dispatch may exist for the resource."
        );

        assertEquals(
                ResourceStatus.BUSY,
                resourceRepository.findById(resourceId).orElseThrow().getStatus()
        );
    }
}