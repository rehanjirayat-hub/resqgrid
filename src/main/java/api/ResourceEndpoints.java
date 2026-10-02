package api;

import com.sun.net.httpserver.HttpExchange;
import model.EmergencyResource;
import model.ResourceStatus;
import model.ResourceType;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static api.ApiServer.ApiResponse;
import static api.ApiServer.parseQuery;
import static api.ApiServer.readBody;

/**
 * Implements the documented resource endpoints under {@code /api/v1/resources}.
 */
final class ResourceEndpoints {

    private ResourceEndpoints() {
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
                return register(context, exchange);
            }

            throw IncidentEndpoints.methodNotAllowed(method);
        }

        long resourceId = RequestFields.pathId(segments, 0);

        if (segments.size() == 1) {

            if (!"GET".equals(method)) {
                throw IncidentEndpoints.methodNotAllowed(method);
            }

            return getById(context, resourceId);
        }

        if (segments.size() == 2 && "status".equals(segments.get(1))) {

            if (!"PUT".equals(method)) {
                throw IncidentEndpoints.methodNotAllowed(method);
            }

            return changeStatus(context, resourceId, exchange);
        }

        throw new ApiException(
                ApiException.ErrorCode.VALIDATION_FAILED,
                "Unknown resource endpoint."
        );
    }

    private static ApiResponse register(ApiContext context, HttpExchange exchange)
            throws IOException {

        Map<String, Object> body = Json.readObject(readBody(exchange));

        ResourceType type = RequestFields.enumValue(
                RequestFields.requiredString(body, "type"),
                ResourceType.class,
                "type"
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

        long id = context.resources().register(type, latitude, longitude);

        return ApiResponse.created(Map.of("id", id));
    }

    private static ApiResponse getById(ApiContext context, long resourceId) {

        EmergencyResource resource = context.resources().findById(resourceId)
                .orElseThrow(() -> new ApiException(
                        ApiException.ErrorCode.RESOURCE_NOT_FOUND,
                        "The resource does not exist."
                ));

        return ApiResponse.ok(toJson(resource));
    }

    private static ApiResponse list(ApiContext context, HttpExchange exchange) {

        Map<String, String> query =
                parseQuery(exchange.getRequestURI().getRawQuery());

        ResourceStatus status = RequestFields.optionalEnumValue(
                query, "status", ResourceStatus.class);

        ResourceType type = RequestFields.optionalEnumValue(
                query, "type", ResourceType.class);

        List<EmergencyResource> resources;

        if (status != null) {
            resources = context.resources().findByStatus(status);
        } else if (type != null) {
            resources = context.resources().findByType(type);
        } else {
            resources = context.resources().findAll();
        }

        return ApiResponse.ok(Map.of(
                "items",
                resources.stream().map(ResourceEndpoints::toJson).toList()
        ));
    }

    private static ApiResponse changeStatus(ApiContext context,
                                            long resourceId,
                                            HttpExchange exchange) throws IOException {

        Map<String, Object> body = Json.readObject(readBody(exchange));

        ResourceStatus status = RequestFields.enumValue(
                RequestFields.requiredString(body, "status"),
                ResourceStatus.class,
                "status"
        );

        if (context.resources().findById(resourceId).isEmpty()) {
            throw new ApiException(
                    ApiException.ErrorCode.RESOURCE_NOT_FOUND,
                    "The resource does not exist."
            );
        }

        context.resources().changeStatus(resourceId, status);

        return ApiResponse.noContent();
    }

    static Map<String, Object> toJson(EmergencyResource resource) {

        Map<String, Object> json = new LinkedHashMap<>();

        json.put("id", resource.getId());
        json.put("type", resource.getType().name());
        json.put("status", resource.getStatus().name());
        json.put("latitude", resource.getLocation().getLatitude());
        json.put("longitude", resource.getLocation().getLongitude());

        return json;
    }
}