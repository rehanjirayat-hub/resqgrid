package exception;

/**
 * Raised when an operation attempts a state transition the documented
 * lifecycle does not permit.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}