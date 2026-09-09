package com.resqgrid.service.dispatch;

import com.resqgrid.domain.dispatch.Dispatch;
import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.incident.IncidentStatus;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.team.ResponseTeam;
import com.resqgrid.domain.team.TeamStatus;

import java.util.Collection;
import java.util.List;

public class ResourceDispatchService {

    private final IncidentResourceRequirementService requirementService;
    private final ResourceSelectionService resourceSelectionService;
    private final ResourceRankingService resourceRankingService;

    public ResourceDispatchService() {
        this.requirementService = new IncidentResourceRequirementService();
        this.resourceSelectionService = new ResourceSelectionService();
        this.resourceRankingService = new ResourceRankingService();
    }

    public Dispatch dispatchIncident(
            Incident incident,
            Collection<EmergencyResource> resources,
            Collection<ResponseTeam> responseTeams
    ) {
        validateInputs(incident, resources, responseTeams);

        if (incident.getStatus() != IncidentStatus.ASSESSED) {
            throw new IllegalStateException(
                    "Incident must be assessed before dispatching"
            );
        }

        ResourceRequirement requirement =
                requirementService.determineRequirement(incident);

        List<EmergencyResource> eligibleResources =
                resourceSelectionService.findEligibleResources(
                        resources,
                        requirement.resourceType(),
                        requirement.capability()
                );

        if (eligibleResources.isEmpty()) {
            throw new IllegalStateException(
                    "No eligible resource is available for incident " + incident.getId()
            );
        }

        List<EmergencyResource> rankedResources =
                resourceRankingService.rankResources(
                        eligibleResources,
                        incident.getLocation()
                );

        ResponseTeam selectedTeam =
                findAvailableTeam(
                        responseTeams,
                        requirement.capability()
                );

        if (selectedTeam == null) {
            throw new IllegalStateException(
                    "No available response team has the required capability"
            );
        }

        /*
         * Try the ranked resources one by one.
         *
         * A resource may become BUSY between the initial selection
         * and the claim operation because another thread may have
         * claimed it.
         */
        for (EmergencyResource resource : rankedResources) {

            Dispatch dispatch = tryClaimResourceAndTeam(
                    resource,
                    selectedTeam,
                    incident
            );

            if (dispatch != null) {
                return dispatch;
            }
        }

        throw new IllegalStateException(
                "No eligible resource is available for incident "
                        + incident.getId()
        );
    }

    private synchronized Dispatch tryClaimResourceAndTeam(
            EmergencyResource resource,
            ResponseTeam team,
            Incident incident
    ) {
        /*
         * The availability check and state change happen
         * inside the same synchronized critical section.
         */
        if (resource.getStatus() != ResourceStatus.AVAILABLE) {
            return null;
        }

        if (team.getStatus() != TeamStatus.AVAILABLE) {
            return null;
        }

        Dispatch dispatch = new Dispatch(
                System.currentTimeMillis(),
                incident
        );

        dispatch.assign(resource, team);

        resource.changeStatus(ResourceStatus.BUSY);
        team.changeStatus(TeamStatus.DEPLOYED);
        incident.changeStatus(IncidentStatus.DISPATCHED);

        return dispatch;
    }

    private ResponseTeam findAvailableTeam(
            Collection<ResponseTeam> responseTeams,
            Capability requiredCapability
    ) {
        return responseTeams.stream()
                .filter(team -> team.getStatus() == TeamStatus.AVAILABLE)
                .filter(team -> team.getCapabilities().contains(requiredCapability))
                .findFirst()
                .orElse(null);
    }

    private void validateInputs(
            Incident incident,
            Collection<EmergencyResource> resources,
            Collection<ResponseTeam> responseTeams
    ) {
        if (incident == null) {
            throw new IllegalArgumentException("Incident cannot be null");
        }

        if (resources == null) {
            throw new IllegalArgumentException("Resources cannot be null");
        }

        if (responseTeams == null) {
            throw new IllegalArgumentException("Response teams cannot be null");
        }
    }
}
