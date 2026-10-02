package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exception.InvalidStateTransitionException;
import exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hosts the documented REST API under {@code /api/v1}.
 *
 * Handlers translate HTTP requests into application-service calls and
 * application results into HTTP responses. No dispatch decision is made here.
 */
public class ApiServer {

    private static final String BASE_PATH = "/api/v1";

    private final HttpServer server;
    private final ApiContext context;

    public ApiServer(ApiContext context, int port) throws IOException {

        this.context = context;

        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext(BASE_PATH, this::handle);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    private void handle(HttpExchange exchange) throws IOException {

        try {

            Route route = Route.parse(
                    exchange.getRequestURI().getPath().substring(BASE_PATH.length())
            );

            ApiResponse response = route.execute(context, exchange);

            send(exchange, response);

        } catch (ApiException e) {

            send(exchange, ApiResponse.error(e.toErrorResponse(), e.getHttpStatus()));

        } catch (InvalidStateTransitionException e) {

            ApiException apiException = new ApiException(
                    ApiException.ErrorCode.INVALID_STATE_TRANSITION,
                    "The requested operation is not permitted in the current state."
            );

            send(exchange, ApiResponse.error(
                    apiException.toErrorResponse(),
                    apiException.getHttpStatus()
            ));

        } catch (ResourceNotFoundException e) {

            ApiException apiException = new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "The requested record does not exist."
            );

            send(exchange, ApiResponse.error(
                    apiException.toErrorResponse(),
                    apiException.getHttpStatus()
            ));

        } catch (IllegalArgumentException e) {

            ApiException apiException = new ApiException(
                    ApiException.ErrorCode.VALIDATION_FAILED,
                    "The request could not be understood."
            );

            send(exchange, ApiResponse.error(
                    apiException.toErrorResponse(),
                    apiException.getHttpStatus()
            ));

        } catch (RuntimeException e) {

            // Internal details are never returned to the client.
            ApiException apiException = new ApiException(
                    ApiException.ErrorCode.INTERNAL_ERROR,
                    "An unexpected error occurred."
            );

            send(exchange, ApiResponse.error(
                    apiException.toErrorResponse(),
                    apiException.getHttpStatus()
            ));

        } finally {
            exchange.close();
        }
    }

    private void send(HttpExchange exchange, ApiResponse response)
            throws IOException {

        byte[] body = response.body() == null
                ? new byte[0]
                : response.body().getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json; charset=utf-8"
        );

        if (response.status() == 204) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        exchange.sendResponseHeaders(response.status(), body.length);

        if (body.length > 0) {

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        }
    }

    static String readBody(HttpExchange exchange) throws IOException {

        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * A single API response: an HTTP status and an optional JSON body.
     */
    record ApiResponse(int status, String body) {

        static ApiResponse ok(Object payload) {
            return new ApiResponse(200, Json.write(payload));
        }

        static ApiResponse created(Object payload) {
            return new ApiResponse(201, Json.write(payload));
        }

        static ApiResponse noContent() {
            return new ApiResponse(204, null);
        }

        static ApiResponse error(Map<String, Object> payload, int status) {
            return new ApiResponse(status, Json.write(payload));
        }
    }

    /**
     * Maps a request path and method onto the documented endpoints.
     */
    private record Route(List<String> segments, String method) {

        static Route parse(String path) {

            List<String> segments = new ArrayList<>();

            for (String segment : path.split("/")) {

                if (!segment.isEmpty()) {
                    segments.add(segment);
                }
            }

            return new Route(segments, "");
        }

        private String methodOf(HttpExchange exchange) {
            return exchange.getRequestMethod();
        }

        ApiResponse execute(ApiContext context, HttpExchange exchange)
                throws IOException {

            String method = methodOf(exchange);

            if (segments.isEmpty()) {
                throw new ApiException(
                        ApiException.ErrorCode.VALIDATION_FAILED,
                        "An endpoint is required."
                );
            }

            return switch (segments.get(0)) {

                case "incidents" -> IncidentEndpoints.handle(
                        segments.subList(1, segments.size()), method, context, exchange);

                case "resources" -> ResourceEndpoints.handle(
                        segments.subList(1, segments.size()), method, context, exchange);

                case "dispatches" -> DispatchEndpoints.handle(
                        segments.subList(1, segments.size()), method, context, exchange);

                case "reports" -> ReportEndpoints.handle(
                        segments.subList(1, segments.size()), method, context, exchange);

                default -> throw new ApiException(
                        ApiException.ErrorCode.VALIDATION_FAILED,
                        "Unknown endpoint."
                );
            };
        }
    }

    static Map<String, String> parseQuery(String rawQuery) {

        Map<String, String> parameters = new HashMap<>();

        if (rawQuery == null || rawQuery.isEmpty()) {
            return parameters;
        }

        for (String pair : rawQuery.split("&")) {

            int separator = pair.indexOf('=');

            if (separator < 0) {
                parameters.put(pair, "");
            } else {
                parameters.put(
                        pair.substring(0, separator),
                        pair.substring(separator + 1)
                );
            }
        }

        return parameters;
    }
}