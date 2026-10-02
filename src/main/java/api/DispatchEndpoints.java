package api;

import com.sun.net.httpserver.HttpExchange;
import model.Dispatch;
import model.ResourceType;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static api.ApiServer.ApiResponse;
import static api.ApiServer.readBody;

/**
 * Implements the documented dispatch endpoints under {@code /api/v1/dispatches}.
 */
final class DispatchEndpoints {

    private DispatchEndpoints() {
    }

    static ApiResponse handle(List<String> segments,
                              String method,
                              ApiContext context,
                              HttpExchange exchange) throws IOException {

        if (segments.isEmpty()) {

            if ("GET".equals(method)) {
                return list(context);
            }

            if ("POST".equals(method)) {
                return assign(context, exchange);
            }

            throw IncidentEndpoints.methodNotAllowed(method);
        }

        long dispatchId = RequestFields.pathId(segments, 0);

        if (segments.size() == 1) {

            if (!"GET".equals(method)) {
                throw IncidentEndpoints.methodNotAllowed(method);
            }

            return getById(context, dispatchId);
        }

        if (segments.size() == 2 && "start".equals(segments.get(1))) {

            if (!"POST".equals(method)) {
                throw IncidentEndpoints.methodNotAllowed(method);
            }

            context.dispatches().start(requireDispatch(context, dispatchId));

            return ApiResponse.noContent();
        }

        if (segments.size() == 2 && "complete".equals(segments.get(1))) {

            if (!"POST".equals(method)) {
                throw IncidentEndpoints.methodNotAllowed(method);
            }

            context.dispatches().complete(requireDispatch(context, dispatchId));

            return ApiResponse.noContent();
        }

        throw new ApiException(
                ApiException.ErrorCode.VALIDATION_FAILED,
                "Unknown dispatch endpoint."
        );
    }

    private static long requireDispatch(ApiContext context, long dispatchId) {

        if (context.dispatches().findById(dispatchId).isEmpty()) {
            throw new ApiException(
                    ApiException.ErrorCode.DISPATCH_NOT_FOUND,
                    "The dispatch does not exist."
            );
        }

        return dispatchId;
    }

    private static ApiResponse assign(ApiContext context, HttpExchange exchange)
            throws IOException {

        Map<String, Object> body = Json.readObject(readBody(exchange));

        long incidentId = RequestFields.requiredLong(body, "incidentId");

        ResourceType requiredType = RequestFields.enumValue(
                RequestFields.requiredString(body, "requiredType"),
                ResourceType.class,
                "requiredType"
        );

        if (context.incidents().findById(incidentId).isEmpty()) {
            throw new ApiException(
                    ApiException.ErrorCode.INCIDENT_NOT_FOUND,
                    "The incident does not exist."
            );
        }

        Dispatch dispatch = context.dispatches()
                                   .assign(incidentId, requiredType)
                                   .orElseThrow(() -> new ApiException(
                                           ApiException.ErrorCode.RESOURCE_UNAVAILABLE,
                                           "No eligible resource is available for this incident."
                                   ));

        return ApiResponse.created(toJson(dispatch));
    }

    private static ApiResponse getById(ApiContext context, long dispatchId) {

        Dispatch dispatch = context.dispatches().findById(dispatchId)
                .orElseThrow(() -> new ApiException(
                        ApiException.ErrorCode.DISPATCH_NOT_FOUND,
                        "The dispatch does not exist."
                ));

        return ApiResponse.ok(toJson(dispatch));
    }

    private static ApiResponse list(ApiContext context) {

        List<Map<String, Object>> items = context.dispatches().findAll()
                .stream()
                .map(DispatchEndpoints::toJson)
                .toList();

        return ApiResponse.ok(Map.of("items", items));
    }

    static Map<String, Object> toJson(Dispatch dispatch) {

        Map<String, Object> json = new LinkedHashMap<>();

        json.put("id", dispatch.getId());
        json.put("incidentId", dispatch.getIncident().getId());
        json.put("resourceId", dispatch.getResource().getId());
        json.put("status", dispatch.getStatus().name());
        json.put("createdAt", dispatch.getCreatedAt().toString());

        return json;
    }
}