package api;

import java.util.List;
import java.util.Map;

/**
 * Shared request parsing and validation for the API handlers.
 *
 * Validation happens before the application service is called, but it does
 * not replace business validation, which remains in the service layer.
 */
final class RequestFields {

    private RequestFields() {
    }

    static String requiredString(Map<String, Object> body, String field) {

        Object value = body.get(field);

        if (value == null || value.toString().isBlank()) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Field '" + field + "' is required."
            );
        }

        return value.toString();
    }

    static double requiredDouble(Map<String, Object> body, String field) {

        Object value = body.get(field);

        if (value == null) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Field '" + field + "' is required."
            );
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Field '" + field + "' must be a number."
            );
        }
    }

    static long requiredLong(Map<String, Object> body, String field) {

        Object value = body.get(field);

        if (value == null) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Field '" + field + "' is required."
            );
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Field '" + field + "' must be an identifier."
            );
        }
    }

    static long pathId(List<String> segments, int index) {

        if (segments.size() <= index) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "An identifier is required."
            );
        }

        try {
            return Long.parseLong(segments.get(index));

        } catch (NumberFormatException e) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "The identifier must be numeric."
            );
        }
    }

    static <T extends Enum<T>> T enumValue(String raw, Class<T> type, String field) {

        if (raw == null || raw.isBlank()) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Query parameter '" + field + "' is required."
            );
        }

        try {
            return Enum.valueOf(type, raw);

        } catch (IllegalArgumentException e) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Value '" + raw + "' is not valid for '" + field + "'."
            );
        }
    }

    static <T extends Enum<T>> T optionalEnumValue(Map<String, String> query,
                                                    String parameter,
                                                    Class<T> type) {

        String raw = query.get(parameter);

        if (raw == null || raw.isBlank()) {
            return null;
        }

        return enumValue(raw, type, parameter);
    }

    static void requireMethod(String actual, String expected) {

        if (!actual.equals(expected)) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "This endpoint does not support " + actual + "."
            );
        }
    }
}