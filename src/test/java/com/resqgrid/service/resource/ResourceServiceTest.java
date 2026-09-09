package com.resqgrid.service.resource;

import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.resource.ResourceType;
import com.resqgrid.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResourceServiceTest {

    private ResourceService resourceService;
    private Location location;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService();

        location = new Location(
                12.9716,
                77.5946,
                "Bengaluru"
        );
    }

    @Test
    void shouldRegisterResourceSuccessfully() {

        EmergencyResource resource = resourceService.registerResource(
                1L,
                "Ambulance-01",
                ResourceType.AMBULANCE,
                location,
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        assertNotNull(resource);
        assertEquals(1L, resource.getId());
        assertEquals("Ambulance-01", resource.getName());
        assertEquals(ResourceType.AMBULANCE, resource.getType());
        assertEquals(location, resource.getLocation());

        assertTrue(
                resource.getCapabilities()
                        .contains(Capability.MEDICAL_RESPONSE)
        );
    }

    @Test
    void shouldRejectDuplicateResourceId() {

        resourceService.registerResource(
                1L,
                "Ambulance-01",
                ResourceType.AMBULANCE,
                location,
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        assertThrows(
                IllegalStateException.class,
                () -> resourceService.registerResource(
                        1L,
                        "Ambulance-02",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                )
        );
    }

    @Test
    void shouldRejectInvalidResourceData() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resourceService.registerResource(
                        0L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                )
        );
    }

    @Test
    void shouldFindRegisteredResourceById() {

        EmergencyResource registeredResource =
                resourceService.registerResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource foundResource =
                resourceService.findResourceById(1L);

        assertNotNull(foundResource);
        assertSame(registeredResource, foundResource);
    }

    @Test
    void shouldThrowExceptionWhenResourceDoesNotExist() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> resourceService.findResourceById(999L)
        );
    }

    @Test
    void shouldReturnTrueWhenResourceIsAvailable() {

        resourceService.registerResource(
                1L,
                "Ambulance-01",
                ResourceType.AMBULANCE,
                location,
                Set.of(Capability.MEDICAL_RESPONSE)
        );

        assertTrue(resourceService.isResourceAvailable(1L));
    }

    @Test
    void shouldThrowExceptionWhenCheckingAvailabilityOfMissingResource() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> resourceService.isResourceAvailable(999L)
        );
    }

    @Test
    void shouldChangeResourceStatusSuccessfully() {

        EmergencyResource resource =
                resourceService.registerResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        resourceService.changeResourceStatus(
                1L,
                ResourceStatus.BUSY
        );

        assertEquals(ResourceStatus.BUSY, resource.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenChangingStatusOfMissingResource() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> resourceService.changeResourceStatus(
                        999L,
                        ResourceStatus.BUSY
                )
        );
    }
}