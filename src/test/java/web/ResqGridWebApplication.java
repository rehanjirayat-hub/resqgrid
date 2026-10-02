package web;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import service.DispatchEngine;
import service.DispatchLifecycleService;
import service.DispatchService;
import service.IncidentLifecycleService;
import service.IncidentService;
import service.ResourceAssignmentService;
import service.ResourceEligibilityFilter;
import service.ResourceRankingService;
import service.ResourceService;
import repository.CapabilityRepository;
import repository.DispatchRepository;
import repository.IncidentRepository;
import repository.LocationRepository;
import repository.ResourceRepository;
import repository.ResponseTeamRepository;

import java.io.File;
import java.nio.file.Files;

/**
 * Embedded Tomcat application that serves the JSP views.
 *
 * Servlets are registered programmatically so the deployment does not depend
 * on a descriptor, and the JSP views are served from the packaged webapp
 * directory.
 */
public class ResqGridWebApplication {

    private static Tomcat tomcat;

    public static void start(int port) throws LifecycleException {

        if (tomcat != null) {
            throw new IllegalStateException("Server is already running.");
        }

        LocationRepository locationRepository = new LocationRepository();

        ResourceRepository resourceRepository =
                new ResourceRepository(locationRepository);

        ResponseTeamRepository responseTeamRepository =
                new ResponseTeamRepository();

        IncidentRepository incidentRepository =
                new IncidentRepository(locationRepository);

        CapabilityRepository capabilityRepository = new CapabilityRepository();

        DispatchRepository dispatchRepository = new DispatchRepository(
                incidentRepository,
                resourceRepository,
                responseTeamRepository
        );

        ResourceAssignmentService assignmentService =
                new ResourceAssignmentService(
                        dispatchRepository,
                        resourceRepository
                );

        DispatchEngine dispatchEngine = new DispatchEngine(
                resourceRepository,
                new ResourceEligibilityFilter(
                        capabilityRepository,
                        dispatchRepository
                ),
                new ResourceRankingService(dispatchRepository),
                assignmentService
        );

        IncidentService incidentService = new IncidentService(
                incidentRepository,
                new IncidentLifecycleService(incidentRepository)
        );

        ResourceService resourceService =
                new ResourceService(resourceRepository);

        DispatchService dispatchService = new DispatchService(
                dispatchRepository,
                incidentRepository,
                resourceRepository,
                dispatchEngine,
                new DispatchLifecycleService(
                        dispatchRepository,
                        resourceRepository
                )
        );

        tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(tempDirectory().getAbsolutePath());

        Context context = tomcat.addWebapp("", webappDirectory().getAbsolutePath());

        Tomcat.addServlet(context, "incidents",
                new IncidentListServlet(incidentService));
        context.addServletMappingDecoded("/incidents", "incidents");

        Tomcat.addServlet(context, "resources",
                new ResourceListServlet(resourceService));
        context.addServletMappingDecoded("/resources", "resources");

        Tomcat.addServlet(context, "dispatches",
                new DispatchListServlet(dispatchService));
        context.addServletMappingDecoded("/dispatches", "dispatches");

        // addWebapp already registers the default and JSP servlets.
        tomcat.start();
    }

    public static int getPort() {
        return tomcat.getConnector().getLocalPort();
    }

    public static void stop() {

        if (tomcat == null) {
            return;
        }

        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (LifecycleException e) {
            throw new IllegalStateException("Failed to stop the server.", e);
        } finally {
            tomcat = null;
        }
    }

    private static File tempDirectory() {

        try {
            return Files.createTempDirectory("resqgrid-tomcat").toFile();

        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to create a work directory.", e);
        }
    }

    private static File webappDirectory() {

        return new File("src/main/webapp");
    }
}