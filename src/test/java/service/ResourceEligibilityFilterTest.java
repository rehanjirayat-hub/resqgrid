package service;

import model.EmergencyResource;
import model.Location;
import model.ResourceCapability;
import model.ResourceStatus;
import model.ResourceType;
import model.Severity;
import model.Incident;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import repository.CapabilityRepository;
import repository.DispatchRepository;
import repository.IncidentRepository;
import repository.LocationRepository;
import repository.ResourceRepository;
import repository.ResponseTeamRepository;
import support.TestDataCleaner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceEligibilityFilterTest {

    private static final LocationRepository locationRepository =
            new LocationRepository();
    private static final CapabilityRepository capabilityRepository =
            new CapabilityRepository();
    private static final ResourceRepository resourceRepository =
            new ResourceRepository(locationRepository);
    private static final ResponseTeamRepository responseTeamRepository =
            new ResponseTeamRepository();
    private static final IncidentRepository incidentRepository =
            new IncidentRepository(locationRepository);
    private static final DispatchRepository dispatchRepository =
            new DispatchRepository(
                    incidentRepository,
                    resourceRepository,
                    responseTeamRepository
            );

    private static ResourceEligibilityFilter filter;

    @BeforeAll
    static void setUpFilter() {

        filter = new ResourceEligibilityFilter(
                capabilityRepository,
                dispatchRepository
        );
    }

    @BeforeEach
    void resetDatabase() {

        TestDataCleaner.clearAll();
    }

    private EmergencyResource createResource(ResourceType type) {

        long id = resourceRepository.save(
                new EmergencyResource(0, type, new Location(12.9716, 77.5946))
        );

        return resourceRepository.findById(id).orElseThrow();
    }

    @Test
    void availableResourceOfRequiredTypeShouldBeEligible() {

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);

        assertTrue(filter.isEligible(
                resource,
                IncidentRequirements.of(ResourceType.AMBULANCE)
        ));
    }

    @Test
    void resourceOfWrongTypeShouldBeRejected() {

        EmergencyResource resource = createResource(ResourceType.FIRE_UNIT);

        assertFalse(filter.isEligible(
                resource,
                IncidentRequirements.of(ResourceType.AMBULANCE)
        ));
    }

    @Test
    void busyResourceShouldBeRejected() {

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);
        resourceRepository.updateStatus(resource.getId(), ResourceStatus.BUSY);

        EmergencyResource busy =
                resourceRepository.findById(resource.getId()).orElseThrow();

        assertFalse(filter.isEligible(
                busy,
                IncidentRequirements.of(ResourceType.AMBULANCE)
        ));
    }

    @Test
    void offlineResourceShouldBeRejected() {

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);
        resourceRepository.updateStatus(resource.getId(), ResourceStatus.OFFLINE);

        EmergencyResource offline =
                resourceRepository.findById(resource.getId()).orElseThrow();

        assertFalse(filter.isEligible(
                offline,
                IncidentRequirements.of(ResourceType.AMBULANCE)
        ));
    }

    @Test
    void resourceMissingRequiredCapabilityShouldBeRejected() {

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);

        assertFalse(filter.isEligible(
                resource,
                IncidentRequirements.of(ResourceType.AMBULANCE)
                                   .requiring(ResourceCapability.MEDICAL_RESPONSE)
        ));
    }

    @Test
    void resourceWithRequiredCapabilityShouldBeEligible() {

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);
        capabilityRepository.addToResource(
                resource.getId(),
                ResourceCapability.MEDICAL_RESPONSE
        );

        assertTrue(filter.isEligible(
                resource,
                IncidentRequirements.of(ResourceType.AMBULANCE)
                                   .requiring(ResourceCapability.MEDICAL_RESPONSE)
        ));
    }

    @Test
    void resourceMissingOneOfSeveralCapabilitiesShouldBeRejected() {

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);
        capabilityRepository.addToResource(
                resource.getId(),
                ResourceCapability.MEDICAL_RESPONSE
        );

        assertFalse(filter.isEligible(
                resource,
                IncidentRequirements.of(ResourceType.AMBULANCE)
                                   .requiring(ResourceCapability.MEDICAL_RESPONSE)
                                   .requiring(ResourceCapability.RESCUE_OPERATION)
        ));
    }

    @Test
    void resourceWithActiveDispatchShouldBeRejected() {

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);

        long incidentId = incidentRepository.save(
                new Incident(
                        "Active dispatch incident",
                        Severity.CRITICAL,
                        new Location(12.9716, 77.5946)
                )
        );

        Incident incident = incidentRepository.findById(incidentId).orElseThrow();

        dispatchRepository.save(
                new model.Dispatch(0, incident, resource, null)
        );

        assertFalse(filter.isEligible(
                resource,
                IncidentRequirements.of(ResourceType.AMBULANCE)
        ));
    }

    @Test
    void resourceWithOnlyCompletedDispatchShouldRemainEligible() {

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);

        long incidentId = incidentRepository.save(
                new Incident(
                        "Completed dispatch incident",
                        Severity.LOW,
                        new Location(12.9716, 77.5946)
                )
        );

        Incident incident = incidentRepository.findById(incidentId).orElseThrow();

        long dispatchId = dispatchRepository.save(
                new model.Dispatch(0, incident, resource, null)
        );

        dispatchRepository.updateStatus(dispatchId, model.DispatchStatus.COMPLETED);

        assertTrue(filter.isEligible(
                resource,
                IncidentRequirements.of(ResourceType.AMBULANCE)
        ));
    }

    @Test
    void filterShouldKeepOnlyEligibleResources() {

        EmergencyResource eligible = createResource(ResourceType.RESCUE_TEAM);
        EmergencyResource wrongType = createResource(ResourceType.FIRE_UNIT);

        resourceRepository.updateStatus(wrongType.getId(), ResourceStatus.MAINTENANCE);

        List<EmergencyResource> result = filter.filter(
                List.of(eligible, wrongType),
                IncidentRequirements.of(ResourceType.RESCUE_TEAM)
        );

        assertTrue(result.stream().anyMatch(r -> r.getId() == eligible.getId()));
        assertFalse(result.stream().anyMatch(r -> r.getId() == wrongType.getId()));
    }
}