package api;

import com.sun.net.httpserver.HttpExchange;
import service.ReportingService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static api.ApiServer.ApiResponse;

/**
 * Implements the documented reporting endpoints under {@code /api/v1/reports}.
 */
final class ReportEndpoints {

    private ReportEndpoints() {
    }

    static ApiResponse handle(List<String> segments,
                              String method,
                              ApiContext context,
                              HttpExchange exchange) {

        if (!"GET".equals(method)) {
            throw IncidentEndpoints.methodNotAllowed(method);
        }

        if (segments.size() != 1) {
            throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Unknown report endpoint."
            );
        }

        return switch (segments.get(0)) {

            case "incidents" -> ApiResponse.ok(incidentReport(context));

            case "dispatches" -> ApiResponse.ok(dispatchReport(context));

            case "resources" -> ApiResponse.ok(resourceReport(context));

            default -> throw new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "Unknown report."
            );
        };
    }

    private static Map<String, Object> incidentReport(ApiContext context) {

        ReportingService.IncidentStatistics stats =
                context.reports().incidentStatistics();

        Map<String, Object> json = new LinkedHashMap<>();

        json.put("totalIncidents", stats.totalIncidents());
        json.put("byStatus", stats.byStatus());
        json.put("bySeverity", stats.bySeverity());

        return json;
    }

    private static Map<String, Object> dispatchReport(ApiContext context) {

        ReportingService.DispatchStatistics stats =
                context.reports().dispatchStatistics();

        Map<String, Object> json = new LinkedHashMap<>();

        json.put("totalDispatches", stats.totalDispatches());
        json.put("activeDispatches", stats.activeDispatches());
        json.put("byStatus", stats.byStatus());

        return json;
    }

    private static Map<String, Object> resourceReport(ApiContext context) {

        ReportingService.ResourceUtilization stats =
                context.reports().resourceUtilization();

        Map<String, Object> workload = new LinkedHashMap<>();
        context.reports().resourceWorkload()
                .forEach((resourceId, count) ->
                        workload.put(String.valueOf(resourceId), count));

        Map<String, Object> json = new LinkedHashMap<>();

        json.put("totalResources", stats.totalResources());
        json.put("byStatus", stats.byStatus());
        json.put("resourcesNotAvailable", stats.resourcesNotAvailable());
        json.put("workload", workload);

        return json;
    }
}