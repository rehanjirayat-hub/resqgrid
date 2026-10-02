package service;

/**
 * Result of a dispatch attempt.
 *
 * The engine reports exactly one of the three documented outcomes and never
 * reports success when no dispatch was created.
 */
public record DispatchOutcome(Status status,
                              Long dispatchId,
                              RankedCandidate selectedCandidate) {

    public enum Status {
        ASSIGNED,
        NO_ELIGIBLE_RESOURCE,
        ALL_CANDIDATES_CLAIMED
    }

    public boolean isAssigned() {
        return status == Status.ASSIGNED;
    }

    static DispatchOutcome assigned(long dispatchId, RankedCandidate candidate) {
        return new DispatchOutcome(Status.ASSIGNED, dispatchId, candidate);
    }

    static DispatchOutcome noEligibleResource() {
        return new DispatchOutcome(Status.NO_ELIGIBLE_RESOURCE, null, null);
    }

    static DispatchOutcome allCandidatesClaimed() {
        return new DispatchOutcome(Status.ALL_CANDIDATES_CLAIMED, null, null);
    }
}