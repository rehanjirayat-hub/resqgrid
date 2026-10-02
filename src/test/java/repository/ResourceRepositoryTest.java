package repository;

import model.EmergencyResource;
import model.Location;
import model.ResourceStatus;
import model.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceRepositoryTest {

    private final LocationRepository locationRepository = new LocationRepository();
    private final ResourceRepository repository =
            new ResourceRepository(locationRepository);

    @Test
    void saveShouldInsertResourceAndReturnGeneratedId() {

        EmergencyResource resource = new EmergencyResource(
                0,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        long id = repository.save(resource);

        assertTrue(id > 0);
    }

    @Test
    void findByIdShouldReturnSavedResource() {

        EmergencyResource resource = new EmergencyResource(
                0,
                ResourceType.FIRE_UNIT,
                new Location(13.0827, 80.2707)
        );

        long id = repository.save(resource);

        Optional<EmergencyResource> result = repository.findById(id);

        assertTrue(result.isPresent());

        EmergencyResource savedResource = result.get();

        assertEquals(id, savedResource.getId());
        assertEquals(ResourceType.FIRE_UNIT, savedResource.getType());
        assertEquals(ResourceStatus.AVAILABLE, savedResource.getStatus());
        assertEquals(13.0827, savedResource.getLocation().getLatitude());
        assertEquals(80.2707, savedResource.getLocation().getLongitude());
    }

    @Test
    void findByIdShouldReturnEmptyForUnknownId() {

        Optional<EmergencyResource> result = repository.findById(-1);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByStatusShouldReturnOnlyMatchingResources() {

        long id = repository.save(new EmergencyResource(
                0,
                ResourceType.RESCUE_TEAM,
                new Location(19.0760, 72.8777)
        ));

        List<EmergencyResource> result =
                repository.findByStatus(ResourceStatus.AVAILABLE);

        assertTrue(result.stream().anyMatch(resource -> resource.getId() == id));
    }

    @Test
    void updateStatusShouldChangeStoredStatus() {

        EmergencyResource resource = new EmergencyResource(
                0,
                ResourceType.AMBULANCE,
                new Location(28.6139, 77.2090)
        );

        long id = repository.save(resource);

        repository.updateStatus(id, ResourceStatus.MAINTENANCE);

        EmergencyResource updatedResource = repository.findById(id).orElseThrow();

        assertEquals(ResourceStatus.MAINTENANCE, updatedResource.getStatus());
        assertTrue(repository.findByStatus(ResourceStatus.AVAILABLE)
                               .stream()
                               .noneMatch(available -> available.getId() == id));
    }

    @Test
    void updateStatusShouldRejectUnknownResource() {

        try {
            repository.updateStatus(-1, ResourceStatus.OFFLINE);
        } catch (RuntimeException e) {
            return;
        }

        throw new AssertionError("Expected unknown resource to be rejected.");
    }
}
