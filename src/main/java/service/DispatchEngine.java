package service;

import model.Incident;
import model.ResponseTeam;
import repository.ResourceRepository;

import java.util.List;

/**
 * Selects the most appropriate resource for an incident and assigns it.
 *
 * The engine runs in the documented order:
 *
 * <pre>
 * Candidate identification
 *         |
 * Eligibility filtering
 *         |
 * Ranking
 *         |
 * Final assignment attempt
 * </pre>
 *
 * It never selects the first available resource. Ranking decides which
 * eligible candidate is attempted first, and a candidate is only assigned
 * after the conditional state transition confirms it is still available.
 */
public class DispatchEngine {

    private final ResourceRepository resourceRepository;
    private final ResourceEligibilityFilter eligibilityFilter;
    private final ResourceRankingService rankingService;
    private final ResourceAssignmentService assignmentService;

    public DispatchEngine(ResourceRepository resourceRepository,
                          ResourceEligibilityFilter eligibilityFilter,
                          ResourceRankingService rankingService,
                          ResourceAssignmentService assignmentService) {

        this.resourceRepository = resourceRepository;
        this.eligibilityFilter = eligibilityFilter;
        this.rankingService = rankingService;
        this.assignmentService = assignmentService;
    }

    public DispatchOutcome dispatch(Incident incident,
                                    IncidentRequirements requirements,
                                    ResponseTeam responseTeam) {

        List<model.EmergencyResource> candidates =
                resourceRepository.findByStatus(model.ResourceStatus.AVAILABLE);

        List<model.EmergencyResource> eligible =
                eligibilityFilter.filter(candidates, requirements);

        if (eligible.isEmpty()) {
            return DispatchOutcome.noEligibleResource();
        }

        List<RankedCandidate> ranked = rankingService.rank(incident, eligible);

        // Each eligible candidate is attempted at most once, so the number of
        // attempts can never exceed the number of eligible resources.
        for (RankedCandidate candidate : ranked) {

            AssignmentResult result = assignmentService.assign(
                    incident,
                    candidate.getResource(),
                    responseTeam
            );

            if (result.assigned()) {
                return DispatchOutcome.assigned(
                        result.dispatchId(),
                        candidate
                );
            }
        }

        return DispatchOutcome.allCandidatesClaimed();
    }
}