package com.resqgrid.service.dispatch;

import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResourceRankingServiceTest {

    private ResourceRankingService rankingService;

    private Location incidentLocation;

    @BeforeEach
    void setUp() {

        rankingService = new ResourceRankingService();

        incidentLocation = new Location(
                12.9716,
                77.5946,
                "Bengaluru"
        );
    }

    @Test
    void shouldRankResourcesFromNearestToFarthest() {

        EmergencyResource farResource =
                new EmergencyResource(
                        1L,
                        "Ambulance-Far",
                        ResourceType.AMBULANCE,
                        new Location(
                                12.8500,
                                77.7000,
                                "Far Location"
                        ),
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource nearResource =
                new EmergencyResource(
                        2L,
                        "Ambulance-Near",
                        ResourceType.AMBULANCE,
                        new Location(
                                12.9700,
                                77.5900,
                                "Near Location"
                        ),
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource mediumResource =
                new EmergencyResource(
                        3L,
                        "Ambulance-Medium",
                        ResourceType.AMBULANCE,
                        new Location(
                                12.9200,
                                77.6500,
                                "Medium Location"
                        ),
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        List<EmergencyResource> result =
                rankingService.rankResources(
                        List.of(
                                farResource,
                                nearResource,
                                mediumResource
                        ),
                        incidentLocation
                );

        assertEquals(3, result.size());

        assertEquals(nearResource, result.get(0));
        assertEquals(mediumResource, result.get(1));
        assertEquals(farResource, result.get(2));
    }

    @Test
    void shouldReturnEmptyListWhenNoResourcesExist() {

        List<EmergencyResource> result =
                rankingService.rankResources(
                        List.of(),
                        incidentLocation
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotModifyOriginalCollectionOrder() {

        EmergencyResource firstResource =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        new Location(
                                12.8500,
                                77.7000,
                                "Far Location"
                        ),
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource secondResource =
                new EmergencyResource(
                        2L,
                        "Ambulance-02",
                        ResourceType.AMBULANCE,
                        new Location(
                                12.9700,
                                77.5900,
                                "Near Location"
                        ),
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        List<EmergencyResource> resources =
                List.of(firstResource, secondResource);

        rankingService.rankResources(
                resources,
                incidentLocation
        );

        assertEquals(firstResource, resources.get(0));
        assertEquals(secondResource, resources.get(1));
    }

    @Test
    void shouldUseResourceIdAsTieBreaker() {

        EmergencyResource resourceOne =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        incidentLocation,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        EmergencyResource resourceTwo =
                new EmergencyResource(
                        2L,
                        "Ambulance-02",
                        ResourceType.AMBULANCE,
                        incidentLocation,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        List<EmergencyResource> result =
                rankingService.rankResources(
                        List.of(resourceTwo, resourceOne),
                        incidentLocation
                );

        assertEquals(resourceOne, result.get(0));
        assertEquals(resourceTwo, result.get(1));
    }

    @Test
    void shouldRejectNullResources() {

        assertThrows(
                IllegalArgumentException.class,
                () -> rankingService.rankResources(
                        null,
                        incidentLocation
                )
        );
    }

    @Test
    void shouldRejectNullIncidentLocation() {

        EmergencyResource resource =
                new EmergencyResource(
                        1L,
                        "Ambulance-01",
                        ResourceType.AMBULANCE,
                        incidentLocation,
                        Set.of(Capability.MEDICAL_RESPONSE)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> rankingService.rankResources(
                        List.of(resource),
                        null
                )
        );
    }
}