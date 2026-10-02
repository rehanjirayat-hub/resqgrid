package service;

import model.Dispatch;
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
import repository.ReportRepository;
import repository.ResourceRepository;
import repository.ResponseTeamRepository;
import support.TestDataCleaner;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportingServiceTest {

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

    private final DispatchLifecycleService dispatchLifecycleService =
            new DispatchLifecycleService(dispatchRepository, resourceRepository);

    private ReportingService reportingService;

    @BeforeEach
    void setUp() {

        TestDataCleaner.clearAll();

        reportingService = new ReportingService(
                new ReportRepository(),
                resourceRepository
        );
    }

    private long createIncident(Severity severity) {

        return incidentRepository.save(
                new Incident("Reported incident", severity,
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

    @Test
    void incidentStatisticsShouldReportTotalsAndBreakdowns() {

        createIncident(Severity.CRITICAL);
        createIncident(Severity.LOW);
        createIncident(Severity.LOW);

        ReportingService.IncidentStatistics stats =
                reportingService.incidentStatistics();

        assertEquals(3, stats.totalIncidents());
        assertEquals(2, stats.bySeverity().get("LOW"));
        assertEquals(1, stats.bySeverity().get("CRITICAL"));
        assertEquals(3, stats.byStatus().get("REPORTED"));
    }

    @Test
    void incidentStatisticsShouldBeZeroForEmptyDatabase() {

        ReportingService.IncidentStatistics stats =
                reportingService.incidentStatistics();

        assertEquals(0, stats.totalIncidents());
        assertTrue(stats.byStatus().isEmpty());
    }

    @Test
    void dispatchStatisticsShouldCountActiveDispatches() {

        Incident incident =
                incidentRepository.findById(createIncident(Severity.HIGH))
                                  .orElseThrow();

        EmergencyResource first = createResource();
        EmergencyResource second = createResource();

        assignmentService.assign(
                incident, first, null
        );

        assignmentService.assign(
                incidentRepository.findById(createIncident(Severity.LOW)).orElseThrow(),
                second,
                null
        );

        ReportingService.DispatchStatistics stats =
                reportingService.dispatchStatistics();

        assertEquals(2, stats.totalDispatches());
        assertEquals(
                2,
                stats.activeDispatches(),
                "CREATED dispatches are active."
        );
        assertEquals(2, stats.byStatus().get("CREATED"));
    }

    @Test
    void dispatchStatisticsShouldExcludeCompletedDispatchesFromActiveCount() {

        Incident incident =
                incidentRepository.findById(createIncident(Severity.HIGH))
                                  .orElseThrow();

        EmergencyResource resource = createResource();

        AssignmentResult result = assignmentService.assign(incident, resource, null);

        dispatchLifecycleService.start(result.dispatchId());
        dispatchLifecycleService.complete(result.dispatchId());

        ReportingService.DispatchStatistics stats =
                reportingService.dispatchStatistics();

        assertEquals(1, stats.totalDispatches());
        assertEquals(0, stats.activeDispatches());
        assertEquals(1, stats.byStatus().get("COMPLETED"));
    }

    @Test
    void resourceUtilizationShouldReportStatusDistribution() {

        createResource();
        EmergencyResource busy = createResource();
        resourceRepository.updateStatus(busy.getId(), ResourceStatus.BUSY);

        ReportingService.ResourceUtilization stats =
                reportingService.resourceUtilization();

        assertEquals(2, stats.totalResources());
        assertEquals(1, stats.byStatus().get("AVAILABLE"));
        assertEquals(1, stats.byStatus().get("BUSY"));
        assertEquals(1, stats.resourcesNotAvailable());
    }

    @Test
    void resourceWorkloadShouldCountAllDispatchesPerResource() {

        Incident incident =
                incidentRepository.findById(createIncident(Severity.HIGH))
                                  .orElseThrow();

        EmergencyResource busy = createResource();
        EmergencyResource idle = createResource();

        assignmentService.assign(incident, busy, null);

        Map<Long, Long> workload = reportingService.resourceWorkload();

        assertEquals(1L, workload.get(busy.getId()));
        assertEquals(0L, workload.get(idle.getId()));
    }

    @Test
    void completedDispatchesShouldBeReportedAsHistory() {

        Incident incident =
                incidentRepository.findById(createIncident(Severity.HIGH))
                                  .orElseThrow();

        EmergencyResource resource = createResource();

        AssignmentResult result = assignmentService.assign(incident, resource, null);

        dispatchLifecycleService.start(result.dispatchId());
        dispatchLifecycleService.complete(result.dispatchId());

        assertEquals(
                1L,
                reportingService.completedDispatchesPerResource().get(resource.getId())
        );
    }

    @Test
    void resourceWorkloadShouldIncludeResourcesWithoutHistory() {

        createResource();

        Map<Long, Long> workload = reportingService.resourceWorkload();

        assertEquals(1, workload.size());
        assertEquals(0L, workload.values().iterator().next());
    }
}