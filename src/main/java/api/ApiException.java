package api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Application-level error codes and the exception the API translates into an
 * HTTP response.
 *
 * Messages carried by this exception are safe to return to clients. Internal
 * details such as SQL statements, credentials and stack traces are never
 * included.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public Map<String, Object> toErrorResponse() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("error", true);
        response.put("code", errorCode.name());
        response.put("message", getMessage());

        return response;
    }

    public enum ErrorCode {

        VALIDATION_FAILED(400),
        INCIDENT_NOT_FOUND(404),
        RESOURCE_NOT_FOUND(404),
        TEAM_NOT_FOUND(404),
        DISPATCH_NOT_FOUND(404),
        INVALID_STATE_TRANSITION(409),
        RESOURCE_UNAVAILABLE(409),
        RESOURCE_NOT_ELIGIBLE(409),
        NO_RESOURCE_AVAILABLE(409),
        DISPATCH_CONFLICT(409),
        PERSISTENCE_FAILURE(500),
        INTERNAL_ERROR(500);

        private final int httpStatus;

        ErrorCode(int httpStatus) {
            this.httpStatus = httpStatus;
        }

        public int getHttpStatus() {
            return httpStatus;
        }
    }
}