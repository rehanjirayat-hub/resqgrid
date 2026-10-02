package api;

import com.sun.net.httpserver.HttpExchange;
import model.Incident;
import model.Severity;
import model.Status;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static api.ApiServer.ApiResponse;
import static api.ApiServer.parseQuery;
import static api.ApiServer.readBody;

/**
 * Implements the documented incident endpoints under {@code /api/v1/incidents}.
 */
final class IncidentEndpoints {

    private IncidentEndpoints() {
    }

    static ApiResponse handle(List<String> segments,
                              String method,
                              ApiContext context,
                              HttpExchange exchange) throws IOException {

        if (segments.isEmpty()) {

            if ("GET".equals(method)) {
                return list(context, exchange);
            }

            if ("POST".equals(method)) {
                return create(context, exchange);
            }

            throw methodNotAllowed(method);
        }

        long incidentId = RequestFields.pathId(segments, 0);

        if (segments.size() == 1) {

            if (!"GET".equals(method)) {
                throw methodNotAllowed(method);
            }

            return getById(context, incidentId);
        }

        if (segments.size() == 2 && "assessment".equals(segments.get(1))) {

            if (!"POST".equals(method)) {
                throw methodNotAllowed(method);
            }

            context.incidents().assess(incidentId);

            return ApiResponse.noContent();
        }

        if (segments.size() == 2 && "resolve".equals(segments.get(1))) {

            if (!"POST".equals(method)) {
                throw methodNotAllowed(method);
            }

            context.incidents().resolve(incidentId);

            return ApiResponse.noContent();
        }

        throw new ApiException(
                ApiException.ErrorCode.VALIDATION_FAILED,
                "Unknown incident endpoint."
        );
    }

    private static ApiResponse create(ApiContext context, HttpExchange exchange)
            throws IOException {

        Map<String, Object> body = Json.readObject(readBody(exchange));

        String description = RequestFields.requiredString(body, "description");

        Severity severity = RequestFields.enumValue(
                RequestFields.requiredString(body, "severity"),
                Severity.class,
                "severity"
        );

        double latitude = RequestFields.requiredDouble(body, "latitude");
        double longitude = RequestFields.requiredDouble(body, "longitude");

        if (latitude < -90 || latitude > 90) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Latitude must be between -90 and 90."
            );
        }

        if (longitude < -180 || longitude > 180) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Longitude must be between -180 and 180."
            );
        }

        long id = context.incidents().create(
                description,
                severity,
                latitude,
                longitude
        );

        return ApiResponse.created(Map.of("id", id));
    }

    private static ApiResponse getById(ApiContext context, long incidentId) {

        Incident incident = context.incidents().findById(incidentId)
                .orElseThrow(() -> new ApiException(
                        ApiException.ErrorCode.INCIDENT_NOT_FOUND,
                        "The incident does not exist."
                ));

        return ApiResponse.ok(toJson(incident));
    }

    private static ApiResponse list(ApiContext context, HttpExchange exchange) {

        Map<String, String> query =
                parseQuery(exchange.getRequestURI().getRawQuery());

        Status status = RequestFields.optionalEnumValue(
                query, "status", Status.class);

        Severity severity = RequestFields.optionalEnumValue(
                query, "severity", Severity.class);

        List<Incident> incidents;

        if (status != null) {
            incidents = context.incidents().findByStatus(status);
        } else {
            incidents = context.incidents().findAll();
        }

        if (severity != null) {
            incidents = incidents.stream()
                    .filter(incident -> incident.getSeverity() == severity)
                    .toList();
        }

        List<Map<String, Object>> items = incidents.stream()
                .map(IncidentEndpoints::toJson)
                .toList();

        return ApiResponse.ok(Map.of("items", items));
    }

    static Map<String, Object> toJson(Incident incident) {

        Map<String, Object> json = new LinkedHashMap<>();

        json.put("id", incident.getId());
        json.put("description", incident.getDescription());
        json.put("severity", incident.getSeverity().name());
        json.put("status", incident.getStatus().name());
        json.put("latitude", incident.getLocation().getLatitude());
        json.put("longitude", incident.getLocation().getLongitude());
        json.put("createdAt", incident.getCreatedAt().toString());

        return json;
    }

    static ApiException methodNotAllowed(String method) {
        return new ApiException(
                ApiException.ErrorCode.VALIDATION_FAILED,
                "This endpoint does not support " + method + "."
        );
    }
}