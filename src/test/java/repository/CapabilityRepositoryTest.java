package repository;

import model.Location;
import model.EmergencyResource;
import model.ResourceCapability;
import model.ResourceType;
import model.ResponseTeam;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CapabilityRepositoryTest {

    private static final CapabilityRepository repository = new CapabilityRepository();

    @BeforeAll
    static void ensureCapabilityCatalogueExists() {

        for (ResourceCapability capability : ResourceCapability.values()) {

            if (repository.findIdByName(capability).isEmpty()) {
                repository.save(capability);
            }
        }
    }

    @Test
    void findIdByNameShouldResolveCataloguedCapability() {

        assertTrue(repository.findIdByName(ResourceCapability.MEDICAL_RESPONSE)
                              .isPresent());
    }

    @Test
    void addToResourceShouldMakeCapabilityDiscoverable() {

        long resourceId = new ResourceRepository(new LocationRepository()).save(
                new EmergencyResource(
                        0,
                        ResourceType.AMBULANCE,
                        new Location(12.9716, 77.5946)
                )
        );

        repository.addToResource(resourceId, ResourceCapability.MEDICAL_RESPONSE);

        List<ResourceCapability> capabilities =
                repository.findByResource(resourceId);

        assertEquals(List.of(ResourceCapability.MEDICAL_RESPONSE), capabilities);
        assertTrue(repository.resourceHasCapability(
                resourceId,
                ResourceCapability.MEDICAL_RESPONSE
        ));
        assertFalse(repository.resourceHasCapability(
                resourceId,
                ResourceCapability.FIRE_RESPONSE
        ));
    }

    @Test
    void addToTeamShouldMakeCapabilityDiscoverable() {

        long teamId = new ResponseTeamRepository().save(new ResponseTeam(0));

        repository.addToTeam(teamId, ResourceCapability.RESCUE_OPERATION);

        assertEquals(
                List.of(ResourceCapability.RESCUE_OPERATION),
                repository.findByTeam(teamId)
        );
        assertTrue(repository.teamHasCapability(
                teamId,
                ResourceCapability.RESCUE_OPERATION
        ));
    }

    @Test
    void duplicateResourceCapabilityShouldBeRejected() {

        long resourceId = new ResourceRepository(new LocationRepository()).save(
                new EmergencyResource(
                        0,
                        ResourceType.AMBULANCE,
                        new Location(13.0827, 80.2707)
                )
        );

        repository.addToResource(resourceId, ResourceCapability.FIRE_RESPONSE);

        assertThrows(
                RuntimeException.class,
                () -> repository.addToResource(
                        resourceId,
                        ResourceCapability.FIRE_RESPONSE
                )
        );
    }

    @Test
    void capabilitiesShouldNotBeLinkedToUnknownResource() {

        assertThrows(
                RuntimeException.class,
                () -> repository.addToResource(
                        -1,
                        ResourceCapability.MEDICAL_RESPONSE
                )
        );
    }
}
