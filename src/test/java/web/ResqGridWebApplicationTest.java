package web;

import model.EmergencyResource;
import model.Incident;
import model.Location;
import model.ResourceType;
import model.Severity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.IncidentRepository;
import repository.LocationRepository;
import repository.ResourceRepository;
import support.TestDataCleaner;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Starts the embedded container and verifies the JSP views are compiled and
 * served with real persisted data.
 *
 * These tests exercise the full request path: container, servlet controller,
 * application service, repository and PostgreSQL, then the JSP view.
 */
public class ResqGridWebApplicationTest {

    private static final LocationRepository locationRepository =
            new LocationRepository();
    private static final ResourceRepository resourceRepository =
            new ResourceRepository(locationRepository);
    private static final IncidentRepository incidentRepository =
            new IncidentRepository(locationRepository);

    private static HttpClient client;
    private static String baseUrl;

    @BeforeAll
    static void startContainer() throws Exception {

        ResqGridWebApplication.start(0);

        client = HttpClient.newHttpClient();

        baseUrl = "http://localhost:" + ResqGridWebApplication.getPort();
    }

    @AfterAll
    static void stopContainer() {

        ResqGridWebApplication.stop();
    }

    @BeforeEach
    void resetDatabase() {

        TestDataCleaner.clearAll();
    }

    private HttpResponse<String> get(String path) throws Exception {

        return client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private long createIncident(String description, Severity severity) {

        return incidentRepository.save(
                new Incident(description, severity,
                        new Location(12.9716, 77.5946))
        );
    }

    private EmergencyResource createResource(ResourceType type) {

        long id = resourceRepository.save(
                new EmergencyResource(0, type, new Location(12.9816, 77.6046))
        );

        return resourceRepository.findById(id).orElseThrow();
    }

    @Test
    void incidentsPageShouldRenderPersistedIncidents() throws Exception {

        createIncident("Building collapse", Severity.CRITICAL);
        createIncident("Minor traffic accident", Severity.LOW);

        HttpResponse<String> response = get("/incidents");

        assertEquals(200, response.statusCode());

        String body = response.body();

        assertTrue(body.contains("Building collapse"), body);
        assertTrue(body.contains("Minor traffic accident"), body);
        assertTrue(body.contains("CRITICAL"), body);
        assertTrue(body.contains("REPORTED"), body);
    }

    @Test
    void incidentsPageShouldFilterByStatus() throws Exception {

        long id = createIncident("Filtered incident", Severity.HIGH);

        HttpResponse<String> filtered = get("/incidents?status=RESOLVED");

        assertEquals(200, filtered.statusCode());

        assertTrue(
                !filtered.body().contains("Filtered incident"),
                "An incident in another status must not be shown."
        );

        assertEquals(200, get("/incidents?status=REPORTED").statusCode());

        assertTrue(get("/incidents?status=REPORTED")
                           .body()
                           .contains("Filtered incident"));
        assertTrue(id > 0);
    }

    @Test
    void incidentsPageShouldRenderWithoutData() throws Exception {

        HttpResponse<String> response = get("/incidents");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("No incidents match"));
    }

    @Test
    void incidentsPageShouldIgnoreUnknownStatusFilter() throws Exception {

        createIncident("Visible incident", Severity.MEDIUM);

        HttpResponse<String> response = get("/incidents?status=NOT_A_STATUS");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Visible incident"));
    }

    @Test
    void resourcesPageShouldRenderPersistedResources() throws Exception {

        createResource(ResourceType.AMBULANCE);
        createResource(ResourceType.FIRE_UNIT);

        HttpResponse<String> response = get("/resources");

        assertEquals(200, response.statusCode());

        String body = response.body();

        assertTrue(body.contains("AMBULANCE"), body);
        assertTrue(body.contains("FIRE_UNIT"), body);
        assertTrue(body.contains("AVAILABLE"), body);
    }

    @Test
    void resourcesPageShouldRenderWithoutData() throws Exception {

        HttpResponse<String> response = get("/resources");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("No resources match"));
    }

    @Test
    void dispatchesPageShouldRenderDispatchHistory() throws Exception {

        long incidentId = createIncident("Dispatched incident", Severity.CRITICAL);

        var incident = incidentRepository.findById(incidentId).orElseThrow();

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);

        service.ResourceAssignmentService assignmentService =
                new service.ResourceAssignmentService(
                        new repository.DispatchRepository(
                                incidentRepository,
                                resourceRepository,
                                new repository.ResponseTeamRepository()
                        ),
                        resourceRepository
                );

        var result = assignmentService.assign(incident, resource, null);

        assertTrue(result.assigned());

        HttpResponse<String> response = get("/dispatches");

        assertEquals(200, response.statusCode());

        String body = response.body();

        assertTrue(body.contains("Dispatched incident"), body);
        assertTrue(body.contains("CREATED"), body);
    }

    @Test
    void dispatchesPageShouldRenderWithoutData() throws Exception {

        HttpResponse<String> response = get("/dispatches");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("no dispatch records"));
    }

    @Test
    void dispatchesPageShouldFilterByResource() throws Exception {

        long incidentId = createIncident("Resource history incident",
                Severity.HIGH);

        var incident = incidentRepository.findById(incidentId).orElseThrow();

        EmergencyResource resource = createResource(ResourceType.AMBULANCE);
        createResource(ResourceType.FIRE_UNIT);

        new service.ResourceAssignmentService(
                new repository.DispatchRepository(
                        incidentRepository,
                        resourceRepository,
                        new repository.ResponseTeamRepository()
                ),
                resourceRepository
        ).assign(incident, resource, null);

        HttpResponse<String> response =
                get("/dispatches?resourceId=" + resource.getId());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Resource history incident"));
    }

    @Test
    void stylesheetShouldBeServed() throws Exception {

        HttpResponse<String> response = get("/css/app.css");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("status-AVAILABLE"));
    }

    @Test
    void jspShouldNotBeDirectlyRequestable() throws Exception {

        HttpResponse<String> response = get("/WEB-INF/views/incidents.jsp");

        assertEquals(404, response.statusCode());
    }

    @Test
    void unknownPathShouldReturnNotFound() throws Exception {

        assertEquals(404, get("/does-not-exist").statusCode());
    }

    @Test
    void malformedFilterShouldStillRenderPage() throws Exception {

        HttpResponse<String> response = get("/dispatches?resourceId=abc");

        assertEquals(200, response.statusCode());
    }
}