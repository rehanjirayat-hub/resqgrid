package com.resqgrid.domain.EmergencyResource;

import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.resource.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyResourceTest {

    private Location createLocation() {
        return new Location(
                12.9716,
                77.5946,
                "Bangalore"
        );
    }

    private EmergencyResource createResource() {
        return new EmergencyResource(
                1L,
                "Ambulance 101",
                ResourceType.AMBULANCE,
                createLocation(),
                Set.of(
                        Capability.MEDICAL_RESPONSE,
                        Capability.RESCUE
                )
        );
    }

    @Test
    void resourceShouldStartAsAvailable() {
        EmergencyResource resource = createResource();

        assertEquals(ResourceStatus.AVAILABLE, resource.getStatus());
    }

    @Test
    void resourceShouldStoreBasicDetails() {
        EmergencyResource resource = createResource();

        assertEquals(1L, resource.getId());
        assertEquals("Ambulance 101", resource.getName());
        assertEquals(ResourceType.AMBULANCE, resource.getType());
        assertEquals("Bangalore", resource.getLocation().getAddress());
    }

    @Test
    void resourceShouldStoreCapabilities() {
        EmergencyResource resource = createResource();

        assertTrue(resource.getCapabilities().contains(
                Capability.MEDICAL_RESPONSE
        ));

        assertTrue(resource.getCapabilities().contains(
                Capability.RESCUE
        ));
    }

    @Test
    void resourceShouldChangeFromAvailableToBusy() {
        EmergencyResource resource = createResource();

        resource.changeStatus(ResourceStatus.BUSY);

        assertEquals(ResourceStatus.BUSY, resource.getStatus());
    }

    @Test
    void resourceShouldReturnToAvailableFromBusy() {
        EmergencyResource resource = createResource();

        resource.changeStatus(ResourceStatus.BUSY);
        resource.changeStatus(ResourceStatus.AVAILABLE);

        assertEquals(ResourceStatus.AVAILABLE, resource.getStatus());
    }

    @Test
    void resourceShouldRejectInvalidStatusTransition() {
        EmergencyResource resource = createResource();

        resource.changeStatus(ResourceStatus.BUSY);

        assertThrows(
                IllegalStateException.class,
                () -> resource.changeStatus(ResourceStatus.MAINTENANCE)
        );
    }

    @Test
    void resourceShouldRejectNullStatus() {
        EmergencyResource resource = createResource();

        assertThrows(
                IllegalArgumentException.class,
                () -> resource.changeStatus(null)
        );
    }

    @Test
    void resourceShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmergencyResource(
                        0L,
                        "Ambulance 101",
                        ResourceType.AMBULANCE,
                        createLocation(),
                        Set.of(Capability.MEDICAL_RESPONSE)
                )
        );
    }

    @Test
    void resourceShouldRejectBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmergencyResource(
                        1L,
                        "   ",
                        ResourceType.AMBULANCE,
                        createLocation(),
                        Set.of(Capability.MEDICAL_RESPONSE)
                )
        );
    }

    @Test
    void resourceShouldRejectNullType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmergencyResource(
                        1L,
                        "Ambulance 101",
                        null,
                        createLocation(),
                        Set.of(Capability.MEDICAL_RESPONSE)
                )
        );
    }

    @Test
    void resourceShouldRejectNullLocation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmergencyResource(
                        1L,
                        "Ambulance 101",
                        ResourceType.AMBULANCE,
                        null,
                        Set.of(Capability.MEDICAL_RESPONSE)
                )
        );
    }

    @Test
    void resourceShouldRejectEmptyCapabilities() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmergencyResource(
                        1L,
                        "Ambulance 101",
                        ResourceType.AMBULANCE,
                        createLocation(),
                        Set.of()
                )
        );
    }

    @Test
    void resourceShouldNotExposeMutableCapabilities() {
        EmergencyResource resource = createResource();

        Set<Capability> capabilities = resource.getCapabilities();

        assertThrows(
                UnsupportedOperationException.class,
                () -> capabilities.clear()
        );

        assertEquals(2, resource.getCapabilities().size());
    }
}