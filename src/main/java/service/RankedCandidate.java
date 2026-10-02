package service;

import model.EmergencyResource;

/**
 * An eligible resource together with the measured factors used to rank it.
 *
 * Keeping these values alongside the resource makes the ranking decision
 * explainable, because the reason one candidate outranks another can be read
 * directly from the candidate data.
 */
public final class RankedCandidate implements Comparable<RankedCandidate> {

    private final EmergencyResource resource;
    private final double distanceInKm;
    private final long workload;

    public RankedCandidate(EmergencyResource resource,
                           double distanceInKm,
                           long workload) {

        this.resource = resource;
        this.distanceInKm = distanceInKm;
        this.workload = workload;
    }

    public EmergencyResource getResource() {
        return resource;
    }

    public double getDistanceInKm() {
        return distanceInKm;
    }

    public long getWorkload() {
        return workload;
    }

    /**
     * Orders candidates by distance, then workload, then resource ID. The ID
     * comparison is the final deterministic tie-breaker, so the result never
     * depends on database or collection ordering.
     */
    @Override
    public int compareTo(RankedCandidate other) {

        int byDistance = Double.compare(distanceInKm, other.distanceInKm);

        if (byDistance != 0) {
            return byDistance;
        }

        int byWorkload = Long.compare(workload, other.workload);

        if (byWorkload != 0) {
            return byWorkload;
        }

        return Long.compare(resource.getId(), other.resource.getId());
    }
}