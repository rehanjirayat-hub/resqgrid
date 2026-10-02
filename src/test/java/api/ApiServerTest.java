package api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.TestDataCleaner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiServerTest {

    private ApiServer server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {

        TestDataCleaner.clearAll();

        server = new ApiServer(new ApiContext(), 0);
        server.start();

        client = HttpClient.newHttpClient();

        baseUrl = "http://localhost:" + server.getPort() + "/api/v1";
    }

    @AfterEach
    void stopServer() {

        if (server != null) {
            server.stop();
        }
    }

    private HttpResponse<String> post(String path, String json) throws Exception {

        return client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private HttpResponse<String> get(String path) throws Exception {

        return client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private HttpResponse<String> put(String path, String json) throws Exception {

        return client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + path))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private long createIncident(String severity) throws Exception {

        HttpResponse<String> response = post(
                "/incidents",
                "{\"description\":\"Test incident\",\"severity\":\"" + severity
                        + "\",\"latitude\":12.9716,\"longitude\":77.5946}"
        );

        assertEquals(201, response.statusCode());

        return longField(response.body(), "id");
    }

    private long createResource(String type, double latitude) throws Exception {

        HttpResponse<String> response = post(
                "/resources",
                "{\"type\":\"" + type + "\",\"latitude\":" + latitude
                        + ",\"longitude\":77.5946}"
        );

        assertEquals(201, response.statusCode());

        return longField(response.body(), "id");
    }

    private long longField(String json, String field) {

        return ((Number) Json.readObject(json).get(field)).longValue();
    }

    @Test
    void createIncidentShouldReturnCreated() throws Exception {

        HttpResponse<String> response = post(
                "/incidents",
                "{\"description\":\"Building fire\",\"severity\":\"HIGH\","
                        + "\"latitude\":12.9716,\"longitude\":77.5946}"
        );

        assertEquals(201, response.statusCode());
        assertTrue(longField(response.body(), "id") > 0);
    }

    @Test
    void getIncidentShouldReturnIncident() throws Exception {

        long id = createIncident("CRITICAL");

        HttpResponse<String> response = get("/incidents/" + id);

        assertEquals(200, response.statusCode());

        Map<String, Object> body = Json.readObject(response.body());

        assertEquals("Test incident", body.get("description"));
        assertEquals("CRITICAL", body.get("severity"));
        assertEquals("REPORTED", body.get("status"));
    }

    @Test
    void getUnknownIncidentShouldReturnNotFound() throws Exception {

        HttpResponse<String> response = get("/incidents/999999");

        assertEquals(404, response.statusCode());
        assertEquals(
                "INCIDENT_NOT_FOUND",
                Json.readObject(response.body()).get("code")
        );
    }

    @Test
    void createIncidentWithMissingSeverityShouldReturnBadRequest() throws Exception {

        HttpResponse<String> response = post(
                "/incidents",
                "{\"description\":\"No severity\",\"latitude\":12.9,"
                        + "\"longitude\":77.5}"
        );

        assertEquals(400, response.statusCode());
        assertEquals(
                "VALIDATION_FAILED",
                Json.readObject(response.body()).get("code")
        );
    }

    @Test
    void createIncidentWithInvalidSeverityShouldReturnBadRequest() throws Exception {

        HttpResponse<String> response = post(
                "/incidents",
                "{\"description\":\"Bad severity\",\"severity\":\"URGENT\","
                        + "\"latitude\":12.9,\"longitude\":77.5}"
        );

        assertEquals(400, response.statusCode());
    }

    @Test
    void createIncidentWithInvalidLatitudeShouldReturnBadRequest() throws Exception {

        HttpResponse<String> response = post(
                "/incidents",
                "{\"description\":\"Bad latitude\",\"severity\":\"LOW\","
                        + "\"latitude\":120.0,\"longitude\":77.5}"
        );

        assertEquals(400, response.statusCode());
    }

    @Test
    void malformedJsonShouldReturnBadRequest() throws Exception {

        HttpResponse<String> response = post("/incidents", "{not json");

        assertEquals(400, response.statusCode());
    }

    @Test
    void listIncidentsShouldReturnCreatedIncidents() throws Exception {

        createIncident("HIGH");
        createIncident("LOW");

        HttpResponse<String> response = get("/incidents");

        assertEquals(200, response.statusCode());

        assertEquals(
                2,
                ((java.util.List<?>) Json.readObject(response.body()).get("items"))
                        .size()
        );
    }

    @Test
    void listIncidentsShouldFilterBySeverity() throws Exception {

        createIncident("HIGH");
        createIncident("LOW");

        HttpResponse<String> response = get("/incidents?severity=LOW");

        Map<String, Object> body = Json.readObject(response.body());

        assertEquals(
                1,
                ((java.util.List<?>) body.get("items")).size()
        );
    }

    @Test
    void assessmentShouldAdvanceIncident() throws Exception {

        long id = createIncident("HIGH");

        HttpResponse<String> response = post("/incidents/" + id + "/assessment", "{}");

        assertEquals(204, response.statusCode());

        Map<String, Object> body = Json.readObject(get("/incidents/" + id).body());

        assertEquals("ASSESSED", body.get("status"));
    }

    @Test
    void invalidStateTransitionShouldReturnConflict() throws Exception {

        long id = createIncident("HIGH");

        post("/incidents/" + id + "/assessment", "{}");

        // Resolving directly from ASSESSED skips a documented stage.
        HttpResponse<String> response = post("/incidents/" + id + "/resolve", "{}");

        assertEquals(409, response.statusCode());
        assertEquals(
                "INVALID_STATE_TRANSITION",
                Json.readObject(response.body()).get("code")
        );
    }

    @Test
    void registerAndFetchResource() throws Exception {

        long id = createResource("AMBULANCE", 12.98);

        HttpResponse<String> response = get("/resources/" + id);

        assertEquals(200, response.statusCode());

        Map<String, Object> body = Json.readObject(response.body());

        assertEquals("AMBULANCE", body.get("type"));
        assertEquals("AVAILABLE", body.get("status"));
    }

    @Test
    void updateResourceStatusShouldReturnNoContent() throws Exception {

        long id = createResource("FIRE_UNIT", 13.08);

        HttpResponse<String> response =
                put("/resources/" + id + "/status", "{\"status\":\"OFFLINE\"}");

        assertEquals(204, response.statusCode());

        Map<String, Object> body = Json.readObject(get("/resources/" + id).body());

        assertEquals("OFFLINE", body.get("status"));
    }

    @Test
    void assignShouldCreateDispatchAndMarkResourceBusy() throws Exception {

        long incidentId = createIncident("CRITICAL");
        long resourceId = createResource("AMBULANCE", 12.9716);

        HttpResponse<String> response = post(
                "/dispatches",
                "{\"incidentId\":" + incidentId + ",\"requiredType\":\"AMBULANCE\"}"
        );

        assertEquals(201, response.statusCode());

        Map<String, Object> dispatch = Json.readObject(response.body());

        assertEquals("CREATED", dispatch.get("status"));
        assertEquals(resourceId, ((Number) dispatch.get("resourceId")).longValue());

        Map<String, Object> resource =
                Json.readObject(get("/resources/" + resourceId).body());

        assertEquals("BUSY", resource.get("status"));
    }

    @Test
    void assignWithoutEligibleResourceShouldReturnConflict() throws Exception {

        long incidentId = createIncident("HIGH");

        createResource("AMBULANCE", 12.97);

        HttpResponse<String> response = post(
                "/dispatches",
                "{\"incidentId\":" + incidentId + ",\"requiredType\":\"FIRE_UNIT\"}"
        );

        assertEquals(409, response.statusCode());
        assertEquals(
                "RESOURCE_UNAVAILABLE",
                Json.readObject(response.body()).get("code")
        );
    }

    @Test
    void assignToUnknownIncidentShouldReturnNotFound() throws Exception {

        createResource("AMBULANCE", 12.97);

        HttpResponse<String> response = post(
                "/dispatches",
                "{\"incidentId\":999999,\"requiredType\":\"AMBULANCE\"}"
        );

        assertEquals(404, response.statusCode());
    }

    @Test
    void dispatchLifecycleShouldProgressAndComplete() throws Exception {

        long incidentId = createIncident("CRITICAL");
        createResource("AMBULANCE", 12.9716);

        long dispatchId = longField(post(
                "/dispatches",
                "{\"incidentId\":" + incidentId + ",\"requiredType\":\"AMBULANCE\"}"
        ).body(), "id");

        assertEquals(204, post("/dispatches/" + dispatchId + "/start", "{}").statusCode());

        assertEquals(
                "IN_PROGRESS",
                Json.readObject(get("/dispatches/" + dispatchId).body()).get("status")
        );

        assertEquals(
                204,
                post("/dispatches/" + dispatchId + "/complete", "{}").statusCode()
        );

        assertEquals(
                "COMPLETED",
                Json.readObject(get("/dispatches/" + dispatchId).body()).get("status")
        );
    }

    @Test
    void startingUnknownDispatchShouldReturnNotFound() throws Exception {

        HttpResponse<String> response = post("/dispatches/999999/start", "{}");

        assertEquals(404, response.statusCode());
        assertEquals(
                "DISPATCH_NOT_FOUND",
                Json.readObject(response.body()).get("code")
        );
    }

    @Test
    void reportEndpointsShouldReturnTotals() throws Exception {

        createIncident("HIGH");
        createResource("AMBULANCE", 12.97);

        assertEquals(200, get("/reports/incidents").statusCode());
        assertEquals(200, get("/reports/dispatches").statusCode());
        assertEquals(200, get("/reports/resources").statusCode());

        Map<String, Object> incidents =
                Json.readObject(get("/reports/incidents").body());

        assertEquals(1L, ((Number) incidents.get("totalIncidents")).longValue());
    }

    @Test
    void errorResponseShouldNotLeakInternalDetails() throws Exception {

        HttpResponse<String> response = post("/incidents", "{not json");

        String body = response.body();

        assertFalse(body.contains("SQLException"));
        assertFalse(body.contains("org.postgresql"));
        assertFalse(body.contains("java.sql"));
        assertFalse(body.toLowerCase().contains("stack"));
    }

    @Test
    void unknownEndpointShouldReturnBadRequest() throws Exception {

        assertEquals(400, get("/does-not-exist").statusCode());
    }

    @Test
    void unsupportedMethodShouldReturnBadRequest() throws Exception {

        long id = createIncident("LOW");

        HttpResponse<String> response = put("/incidents/" + id, "{}");

        assertEquals(400, response.statusCode());
    }
}