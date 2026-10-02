package service;

import model.Incident;
import model.Location;
import model.Severity;
import model.Status;
import repository.IncidentRepository;

import java.util.List;
import java.util.Optional;

/**
 * Application service for incident operations.
 *
 * The REST layer delegates to this service rather than implementing incident
 * rules itself. It owns the mapping between API input and the domain.
 */
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentLifecycleService lifecycleService;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentLifecycleService lifecycleService) {

        this.incidentRepository = incidentRepository;
        this.lifecycleService = lifecycleService;
    }

    public long create(String description, Severity severity,
                       double latitude, double longitude) {

        return incidentRepository.save(
                new Incident(
                        description,
                        severity,
                        new Location(latitude, longitude)
                )
        );
    }

    public Optional<Incident> findById(long id) {
        return incidentRepository.findById(id);
    }

    public List<Incident> findAll() {
        return incidentRepository.findAll();
    }

    public List<Incident> findByStatus(Status status) {
        return incidentRepository.findByStatus(status);
    }

    public void assess(long id) {
        lifecycleService.assess(id);
    }

    public void determineRequirements(long id) {
        lifecycleService.determineRequirements(id);
    }

    public void startResponse(long id) {
        lifecycleService.startResponse(id);
    }

    public void resolve(long id) {
        lifecycleService.resolve(id);
    }
}