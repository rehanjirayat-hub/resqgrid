package service;

import model.Dispatch;
import model.DispatchStatus;
import model.EmergencyResource;
import model.ResourceStatus;
import repository.CapabilityRepository;
import repository.DispatchRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Removes resources that fail mandatory eligibility conditions.
 *
 * Filtering is deliberately separate from ranking. A resource that fails any
 * mandatory condition is rejected outright and is never compared against
 * other candidates, because no ranking advantage can compensate for an
 * ineligible resource.
 */
public class ResourceEligibilityFilter {

    private final CapabilityRepository capabilityRepository;
    private final DispatchRepository dispatchRepository;

    public ResourceEligibilityFilter(CapabilityRepository capabilityRepository,
                                     DispatchRepository dispatchRepository) {

        this.capabilityRepository = capabilityRepository;
        this.dispatchRepository = dispatchRepository;
    }

    public List<EmergencyResource> filter(List<EmergencyResource> candidates,
                                          IncidentRequirements requirements) {

        List<EmergencyResource> eligible = new ArrayList<>();

        for (EmergencyResource candidate : candidates) {

            if (isEligible(candidate, requirements)) {
                eligible.add(candidate);
            }
        }

        return eligible;
    }

    public boolean isEligible(EmergencyResource resource,
                              IncidentRequirements requirements) {

        return hasDispatchableStatus(resource)
                && matchesRequiredType(resource, requirements)
                && hasRequiredCapabilities(resource, requirements)
                && hasNoActiveAssignment(resource);
    }

    private boolean hasDispatchableStatus(EmergencyResource resource) {
        return resource.getStatus() == ResourceStatus.AVAILABLE;
    }

    private boolean matchesRequiredType(EmergencyResource resource,
                                        IncidentRequirements requirements) {

        if (!requirements.hasResourceTypeRequirement()) {
            return true;
        }

        return resource.getType() == requirements.getRequiredResourceType();
    }

    private boolean hasRequiredCapabilities(
            EmergencyResource resource,
            IncidentRequirements requirements) {

        for (var capability : requirements.getRequiredCapabilities()) {

            if (!capabilityRepository.resourceHasCapability(
                    resource.getId(),
                    capability
            )) {
                return false;
            }
        }

        return true;
    }

    /**
     * A resource with a conflicting active dispatch is not available for a new
     * assignment. Completed dispatches are historical and do not block the
     * resource, so only active states are considered here.
     */
    private boolean hasNoActiveAssignment(EmergencyResource resource) {

        Set<DispatchStatus> activeStatuses = Set.of(
                DispatchStatus.CREATED,
                DispatchStatus.IN_PROGRESS
        );

        for (Dispatch dispatch : dispatchRepository.findByResource(resource.getId())) {

            if (activeStatuses.contains(dispatch.getStatus())) {
                return false;
            }
        }

        return true;
    }
}