package service;

import model.Dispatch;
import model.Incident;
import model.ResourceType;
import repository.DispatchRepository;
import repository.IncidentRepository;
import repository.ResourceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Application service for dispatch operations.
 *
 * Assignment is performed by the dispatch engine, which owns the selection
 * rules. This service only loads the referenced records and delegates.
 */
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final IncidentRepository incidentRepository;
    private final ResourceRepository resourceRepository;
    private final DispatchEngine dispatchEngine;
    private final DispatchLifecycleService lifecycleService;

    public DispatchService(DispatchRepository dispatchRepository,
                           IncidentRepository incidentRepository,
                           ResourceRepository resourceRepository,
                           DispatchEngine dispatchEngine,
                           DispatchLifecycleService lifecycleService) {

        this.dispatchRepository = dispatchRepository;
        this.incidentRepository = incidentRepository;
        this.resourceRepository = resourceRepository;
        this.dispatchEngine = dispatchEngine;
        this.lifecycleService = lifecycleService;
    }

    /**
     * Requests dispatch for an incident against the given resource type.
     *
     * @return the created dispatch, or empty when no eligible resource existed
     */
    public Optional<Dispatch> assign(long incidentId, ResourceType requiredType) {

        Incident incident = incidentRepository.findById(incidentId).orElse(null);

        if (incident == null) {
            return Optional.empty();
        }

        DispatchOutcome outcome = dispatchEngine.dispatch(
                incident,
                IncidentRequirements.of(requiredType),
                null
        );

        if (!outcome.isAssigned()) {
            return Optional.empty();
        }

        return dispatchRepository.findById(outcome.dispatchId());
    }

    public Optional<Dispatch> findById(long id) {
        return dispatchRepository.findById(id);
    }

    public List<Dispatch> findAll() {
        return dispatchRepository.findAll();
    }

    public List<Dispatch> findByIncident(long incidentId) {
        return dispatchRepository.findByIncident(incidentId);
    }

    public List<Dispatch> findByResource(long resourceId) {
        return dispatchRepository.findByResource(resourceId);
    }

    public Optional<Dispatch> dispatchById(long dispatchId) {
        return dispatchRepository.findById(dispatchId);
    }

    public void start(long dispatchId) {
        lifecycleService.start(dispatchId);
    }

    public void complete(long dispatchId) {
        lifecycleService.complete(dispatchId);
    }
}