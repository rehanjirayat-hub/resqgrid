package com.resqgrid.service.dispatch;

import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.resource.ResourceType;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ResourceSelectionService {

    public List<EmergencyResource> findAvailableResources(
            Collection<EmergencyResource> resources) {

        Predicate<EmergencyResource> isAvailable =
                resource -> resource.getStatus() == ResourceStatus.AVAILABLE;

        return resources.stream()
                .filter(isAvailable)
                .toList();
    }

    public List<EmergencyResource> findResourcesWithCapability(
            Collection<EmergencyResource> resources,
            Capability requiredCapability) {

        Predicate<EmergencyResource> hasRequiredCapability =
                resource -> resource.getCapabilities()
                        .contains(requiredCapability);

        return resources.stream()
                .filter(hasRequiredCapability)
                .toList();
    }

    public List<EmergencyResource> findResourcesByType(
            Collection<EmergencyResource> resources,
            ResourceType requiredType) {

        Predicate<EmergencyResource> hasRequiredType =
                resource -> resource.getType() == requiredType;

        return resources.stream()
                .filter(hasRequiredType)
                .toList();
    }

    public List<EmergencyResource> findEligibleResources(
            Collection<EmergencyResource> resources,
            ResourceType requiredType,
            Capability requiredCapability) {

        Predicate<EmergencyResource> isAvailable =
                resource -> resource.getStatus() == ResourceStatus.AVAILABLE;

        Predicate<EmergencyResource> hasRequiredType =
                resource -> resource.getType() == requiredType;

        Predicate<EmergencyResource> hasRequiredCapability =
                resource -> resource.getCapabilities()
                        .contains(requiredCapability);

        Predicate<EmergencyResource> isEligible =
                isAvailable
                        .and(hasRequiredType)
                        .and(hasRequiredCapability);

        return resources.stream()
                .filter(isEligible)
                .toList();
    }
}