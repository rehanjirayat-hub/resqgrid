package service;

import model.EmergencyResource;
import model.Incident;
import repository.DispatchRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orders eligible resources so the most suitable candidate is ranked first.
 *
 * Ranking is only ever applied to resources that already passed mandatory
 * eligibility filtering. It compares distance, then workload, and finally
 * resource ID as a deterministic tie-breaker.
 */
public class ResourceRankingService {

    private final DispatchRepository dispatchRepository;

    public ResourceRankingService(DispatchRepository dispatchRepository) {
        this.dispatchRepository = dispatchRepository;
    }

    public List<RankedCandidate> rank(Incident incident,
                                      List<EmergencyResource> eligibleResources) {

        List<RankedCandidate> candidates = new ArrayList<>();

        for (EmergencyResource resource : eligibleResources) {

            candidates.add(new RankedCandidate(
                    resource,
                    DistanceCalculator.distanceInKm(
                            incident.getLocation(),
                            resource.getLocation()
                    ),
                    workloadOf(resource)
            ));
        }

        Collections.sort(candidates);

        return candidates;
    }

    /**
     * Workload is the number of dispatch records the resource has, whether
     * active or completed. It is derived from persisted history rather than
     * stored as an undocumented field.
     */
    private long workloadOf(EmergencyResource resource) {
        return dispatchRepository.findByResource(resource.getId()).size();
    }
}