package com.resqgrid.service.dispatch;

import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResourceSelectionServiceTest {

    private ResourceSelectionService selectionService;
    private Location location;

    @BeforeEach
    void setUp() {
        selectionService = new ResourceSelectionService();

        location = new Location(
                12.9716,
                77.5946,
                "Bengaluru"
        );
    }

    @Test
    void shouldReturnOnlyAvailableResources() {

        EmergencyResource availableResource =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource busyResource =
                new EmergencyResource(
                        2L,
                        "Ambulance-02",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        busyResource.changeStatus(ResourceStatus.BUSY);

        List<EmergencyResource> result =
                selectionService.findAvailableResources(
                        List.of(availableResource, busyResource)
                );

        assertEquals(1, result.size());
        assertTrue(result.contains(availableResource));
        assertFalse(result.contains(busyResource));
    }

    @Test
    void shouldReturnEmptyListWhenNoResourcesAreAvailable() {

        EmergencyResource busyResource =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        busyResource.changeStatus(ResourceStatus.BUSY);

        List<EmergencyResource> result =
                selectionService.findAvailableResources(
                        List.of(busyResource)
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllResourcesWhenAllAreAvailable() {

        EmergencyResource ambulance =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource fireUnit =
                new EmergencyResource(
                        2L,
                        "FireUnit-01",
                        ResourceType.FIRE_UNIT,
                        location,
                        Set.of(Capability.FIRE_RESPONSE)
                );

        List<EmergencyResource> result =
                selectionService.findAvailableResources(
                        List.of(ambulance, fireUnit)
                );

        assertEquals(2, result.size());
        assertTrue(result.contains(ambulance));
        assertTrue(result.contains(fireUnit));
    }

    @Test
    void shouldReturnEmptyListWhenInputCollectionIsEmpty() {

        List<EmergencyResource> result =
                selectionService.findAvailableResources(
                        List.of()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnResourcesWithRequiredCapability() {

        EmergencyResource ambulance =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource fireUnit =
                new EmergencyResource(
                        2L,
                        "FireUnit-01",
                        ResourceType.FIRE_UNIT,
                        location,
                        Set.of(Capability.FIRE_RESPONSE)
                );

        List<EmergencyResource> result =
                selectionService.findResourcesWithCapability(
                        List.of(ambulance, fireUnit),
                        Capability.MEDICAL_RESPONSE
                );

        assertEquals(1, result.size());
        assertTrue(result.contains(ambulance));
        assertFalse(result.contains(fireUnit));
    }

    @Test
    void shouldReturnEmptyListWhenNoResourceHasRequiredCapability() {

        EmergencyResource fireUnit =
                new EmergencyResource(
                        1L,
                        "FireUnit-01",
                        ResourceType.FIRE_UNIT,
                        location,
                        Set.of(Capability.FIRE_RESPONSE)
                );

        List<EmergencyResource> result =
                selectionService.findResourcesWithCapability(
                        List.of(fireUnit),
                        Capability.MEDICAL_RESPONSE
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnResourcesWithRequiredType() {

        EmergencyResource ambulance =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource fireUnit =
                new EmergencyResource(
                        2L,
                        "FireUnit-01",
                        ResourceType.FIRE_UNIT,
                        location,
                        Set.of(Capability.FIRE_RESPONSE)
                );

        List<EmergencyResource> result =
                selectionService.findResourcesByType(
                        List.of(ambulance, fireUnit),
                        ResourceType.AMBULANCE
                );

        assertEquals(1, result.size());
        assertTrue(result.contains(ambulance));
        assertFalse(result.contains(fireUnit));
    }

    @Test
    void shouldReturnEmptyListWhenNoResourceHasRequiredType() {

        EmergencyResource fireUnit =
                new EmergencyResource(
                        1L,
                        "FireUnit-01",
                        ResourceType.FIRE_UNIT,
                        location,
                        Set.of(Capability.FIRE_RESPONSE)
                );

        List<EmergencyResource> result =
                selectionService.findResourcesByType(
                        List.of(fireUnit),
                        ResourceType.AMBULANCE
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnOnlyAvailableResourcesWithRequiredCapability() {

        EmergencyResource eligibleAmbulance =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource busyAmbulance =
                new EmergencyResource(
                        2L,
                        "Ambulance-02",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        busyAmbulance.changeStatus(ResourceStatus.BUSY);

        EmergencyResource availableFireUnit =
                new EmergencyResource(
                        3L,
                        "FireUnit-01",
                        ResourceType.FIRE_UNIT,
                        location,
                        Set.of(Capability.FIRE_RESPONSE)
                );

        List<EmergencyResource> result =
                selectionService.findEligibleResources(
                        List.of(
                                eligibleAmbulance,
                                busyAmbulance,
                                availableFireUnit
                        ),
                        ResourceType.AMBULANCE,
                        Capability.MEDICAL_RESPONSE
                );

        assertEquals(1, result.size());
        assertTrue(result.contains(eligibleAmbulance));
        assertFalse(result.contains(busyAmbulance));
        assertFalse(result.contains(availableFireUnit));
    }

    @Test
    void shouldReturnEmptyListWhenNoResourceIsEligible() {

        EmergencyResource busyAmbulance =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        busyAmbulance.changeStatus(ResourceStatus.BUSY);

        List<EmergencyResource> result =
                selectionService.findEligibleResources(
                        List.of(busyAmbulance),
                        ResourceType.AMBULANCE,
                        Capability.MEDICAL_RESPONSE
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectResourceWithCorrectCapabilityButWrongType() {

        EmergencyResource fireUnit =
                new EmergencyResource(
                        1L,
                        "FireUnit-01",
                        ResourceType.FIRE_UNIT,
                        location,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        List<EmergencyResource> result =
                selectionService.findEligibleResources(
                        List.of(fireUnit),
                        ResourceType.AMBULANCE,
                        Capability.MEDICAL_RESPONSE
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectResourceWithCorrectTypeButWrongCapability() {

        EmergencyResource ambulance =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        location,
                        Set.of(Capability.FIRE_RESPONSE)
                );

        List<EmergencyResource> result =
                selectionService.findEligibleResources(
                        List.of(ambulance),
                        ResourceType.AMBULANCE,
                        Capability.MEDICAL_RESPONSE
                );

        assertTrue(result.isEmpty());
    }
}