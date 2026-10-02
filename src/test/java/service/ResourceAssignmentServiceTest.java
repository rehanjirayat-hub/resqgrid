package service;

import model.EmergencyResource;
import model.Incident;
import model.Location;
import model.ResourceStatus;
import model.ResourceType;
import model.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import repository.DispatchRepository;
import repository.IncidentRepository;
import repository.LocationRepository;
import repository.ResourceRepository;
import repository.ResponseTeamRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceAssignmentServiceTest {

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
    private final ResourceAssignmentService service =
            new ResourceAssignmentService(dispatchRepository, resourceRepository);

    private long createIncident(String description) {

        return incidentRepository.save(new Incident(
                description,
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        ));
    }

    private long createAvailableResource() {

        return resourceRepository.save(new EmergencyResource(
                0,
                ResourceType.AMBULANCE,
                new Location(13.0827, 80.2707)
        ));
    }

    private Incident loadIncident(long id) {
        return incidentRepository.findById(id).orElseThrow();
    }

    private EmergencyResource loadResource(long id) {
        return resourceRepository.findById(id).orElseThrow();
    }

    @Test
    void assignShouldCreateDispatchAndMarkResourceBusy() {

        long incidentId = createIncident("Solo incident");
        long resourceId = createAvailableResource();

        AssignmentResult result = service.assign(
                loadIncident(incidentId),
                loadResource(resourceId),
                null
        );

        assertTrue(result.assigned());
        assertEquals(
                ResourceStatus.BUSY,
                resourceRepository.findById(resourceId).orElseThrow().getStatus()
        );
        assertEquals(1, dispatchRepository.findByResource(resourceId).size());
    }

    @Test
    void assignShouldRejectResourceThatIsNoLongerAvailable() {

        long incidentId = createIncident("Second incident");
        long resourceId = createAvailableResource();

        resourceRepository.updateStatus(resourceId, ResourceStatus.OFFLINE);

        AssignmentResult result = service.assign(
                loadIncident(incidentId),
                loadResource(resourceId),
                null
        );

        assertFalse(result.assigned());
        assertTrue(dispatchRepository.findByResource(resourceId).isEmpty());
    }

    @Test
    void assignShouldRejectSecondIncidentWhenResourceAlreadyAssigned() {

        long firstIncidentId = createIncident("First incident");
        long secondIncidentId = createIncident("Second incident");
        long resourceId = createAvailableResource();

        AssignmentResult first = service.assign(
                loadIncident(firstIncidentId),
                loadResource(resourceId),
                null
        );

        AssignmentResult second = service.assign(
                loadIncident(secondIncidentId),
                loadResource(resourceId),
                null
        );

        assertTrue(first.assigned());
        assertFalse(second.assigned());
        assertEquals(1, dispatchRepository.findByResource(resourceId).size());
    }

    @Test
    void concurrentAssignShouldSucceedExactlyOnce() throws Exception {

        long firstIncidentId = createIncident("Race incident A");
        long secondIncidentId = createIncident("Race incident B");
        long resourceId = createAvailableResource();

        int attempts = 8;

        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(attempts);

        try {

            for (int attempt = 0; attempt < attempts; attempt++) {

                long incidentId =
                        attempt % 2 == 0 ? firstIncidentId : secondIncidentId;

                executor.submit(() -> {

                    ready.countDown();
                    start.await();

                    AssignmentResult result = service.assign(
                            loadIncident(incidentId),
                            loadResource(resourceId),
                            null
                    );

                    if (result.assigned()) {
                        succeeded.incrementAndGet();
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

        assertEquals(1, succeeded.get(), "Exactly one assignment may succeed.");

        List<model.Dispatch> dispatches =
                dispatchRepository.findByResource(resourceId);

        assertEquals(1, dispatches.size(), "Only one active dispatch may exist.");

        assertEquals(
                ResourceStatus.BUSY,
                resourceRepository.findById(resourceId).orElseThrow().getStatus()
        );
    }
}