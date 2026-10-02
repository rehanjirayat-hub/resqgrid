package service;

import model.EmergencyResource;
import model.Location;
import model.ResourceStatus;
import model.ResourceType;
import repository.ResourceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Application service for emergency resource operations.
 *
 * Status changes requested through the API are recorded as given; deciding
 * whether a resource may be dispatched remains the responsibility of the
 * dispatch engine.
 */
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public long register(ResourceType type, double latitude, double longitude) {

        return resourceRepository.save(
                new EmergencyResource(
                        0,
                        type,
                        new Location(latitude, longitude)
                )
        );
    }

    public Optional<EmergencyResource> findById(long id) {
        return resourceRepository.findById(id);
    }

    public List<EmergencyResource> findAll() {
        return resourceRepository.findAll();
    }

    public List<EmergencyResource> findByStatus(ResourceStatus status) {
        return resourceRepository.findByStatus(status);
    }

    public List<EmergencyResource> findByType(ResourceType type) {

        return resourceRepository.findAll().stream()
                .filter(resource -> resource.getType() == type)
                .toList();
    }

    public void changeStatus(long id, ResourceStatus status) {
        resourceRepository.updateStatus(id, status);
    }
}