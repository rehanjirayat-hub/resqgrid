package api;

import repository.CapabilityRepository;
import repository.DispatchRepository;
import repository.IncidentRepository;
import repository.LocationRepository;
import repository.ReportRepository;
import repository.ResourceRepository;
import repository.ResponseTeamRepository;
import service.DispatchEngine;
import service.DispatchLifecycleService;
import service.DispatchService;
import service.IncidentLifecycleService;
import service.IncidentService;
import service.ReportingService;
import service.ResourceAssignmentService;
import service.ResourceEligibilityFilter;
import service.ResourceRankingService;
import service.ResourceService;

/**
 * Wires the application's repositories, services and API handlers together.
 *
 * Composition is kept in one place so handlers can stay focused on
 * translating HTTP requests into application operations.
 */
public class ApiContext {

    private final IncidentService incidentService;
    private final ResourceService resourceService;
    private final DispatchService dispatchService;
    private final ReportingService reportingService;

    public ApiContext() {
        this(new LocationRepository());
    }

    public ApiContext(LocationRepository locationRepository) {

        ResourceRepository resourceRepository =
                new ResourceRepository(locationRepository);

        ResponseTeamRepository responseTeamRepository = new ResponseTeamRepository();

        IncidentRepository incidentRepository =
                new IncidentRepository(locationRepository);

        CapabilityRepository capabilityRepository = new CapabilityRepository();

        DispatchRepository dispatchRepository = new DispatchRepository(
                incidentRepository,
                resourceRepository,
                responseTeamRepository
        );

        ResourceAssignmentService assignmentService =
                new ResourceAssignmentService(
                        dispatchRepository,
                        resourceRepository
                );

        DispatchEngine dispatchEngine = new DispatchEngine(
                resourceRepository,
                new ResourceEligibilityFilter(
                        capabilityRepository,
                        dispatchRepository
                ),
                new ResourceRankingService(dispatchRepository),
                assignmentService
        );

        this.incidentService = new IncidentService(
                incidentRepository,
                new IncidentLifecycleService(incidentRepository)
        );

        this.resourceService = new ResourceService(resourceRepository);

        this.dispatchService = new DispatchService(
                dispatchRepository,
                incidentRepository,
                resourceRepository,
                dispatchEngine,
                new DispatchLifecycleService(
                        dispatchRepository,
                        resourceRepository
                )
        );

        this.reportingService = new ReportingService(
                new ReportRepository(),
                resourceRepository
        );
    }

    public IncidentService incidents() {
        return incidentService;
    }

    public ResourceService resources() {
        return resourceService;
    }

    public DispatchService dispatches() {
        return dispatchService;
    }

    public ReportingService reports() {
        return reportingService;
    }
}