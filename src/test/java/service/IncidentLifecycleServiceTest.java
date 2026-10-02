package service;

import model.Incident;
import model.Location;
import model.Severity;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import repository.IncidentRepository;
import repository.LocationRepository;
import support.TestDataCleaner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IncidentLifecycleServiceTest {

    private final LocationRepository locationRepository = new LocationRepository();
    private final IncidentRepository incidentRepository =
            new IncidentRepository(locationRepository);
    private final IncidentLifecycleService lifecycleService =
            new IncidentLifecycleService(incidentRepository);

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

    private Status statusOf(long incidentId) {
        return incidentRepository.findById(incidentId).orElseThrow().getStatus();
    }

    @Test
    void newIncidentShouldBeReported() {

        assertEquals(Status.REPORTED, statusOf(createIncident()));
    }

    @Test
    void assessShouldMoveIncidentToAssessed() {

        long id = createIncident();

        lifecycleService.assess(id);

        assertEquals(Status.ASSESSED, statusOf(id));
    }

    @Test
    void fullLifecycleShouldReachResolved() {

        long id = createIncident();

        lifecycleService.assess(id);
        lifecycleService.determineRequirements(id);
        lifecycleService.startResponse(id);
        lifecycleService.resolve(id);

        assertEquals(Status.RESOLVED, statusOf(id));
    }

    @Test
    void assessShouldRejectIncidentThatIsAlreadyAssessed() {

        long id = createIncident();

        lifecycleService.assess(id);

        assertThrows(IllegalStateException.class, () -> lifecycleService.assess(id));
    }

    @Test
    void determineRequirementsShouldRejectReportedIncident() {

        long id = createIncident();

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.determineRequirements(id)
        );
    }

    @Test
    void resolveShouldRejectIncidentThatHasNotStartedResponding() {

        long id = createIncident();

        lifecycleService.assess(id);
        lifecycleService.determineRequirements(id);

        assertThrows(IllegalStateException.class, () -> lifecycleService.resolve(id));
    }

    @Test
    void rejectedTransitionShouldNotChangeStatus() {

        long id = createIncident();

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.determineRequirements(id)
        );

        assertEquals(
                Status.REPORTED,
                statusOf(id),
                "A rejected transition must leave the incident unchanged."
        );
    }

    @Test
    void transitionsShouldRejectUnknownIncident() {

        assertThrows(
                IllegalStateException.class,
                () -> lifecycleService.assess(-1)
        );
    }
}