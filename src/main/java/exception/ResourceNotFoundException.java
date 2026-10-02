package exception;

/**
 * Raised when a requested record does not exist.
 *
 * This is distinct from a business-rule failure so callers can report it as a
 * missing resource rather than a conflict.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}