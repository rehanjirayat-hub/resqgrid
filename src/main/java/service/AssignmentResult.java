package service;

/**
 * Outcome of an attempt to assign a resource to an incident.
 *
 * A rejection means the resource was no longer available at the moment of
 * assignment, which normally means another incident claimed it first.
 */
public record AssignmentResult(Long dispatchId, Long resourceId, boolean assigned) {

    static AssignmentResult assigned(long dispatchId, long resourceId) {
        return new AssignmentResult(dispatchId, resourceId, true);
    }

    static AssignmentResult rejected(long resourceId) {
        return new AssignmentResult(null, resourceId, false);
    }
}