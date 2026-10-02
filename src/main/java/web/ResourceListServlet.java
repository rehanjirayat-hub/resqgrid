package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.EmergencyResource;
import model.ResourceStatus;
import service.ResourceService;

import java.io.IOException;
import java.util.List;

/**
 * Displays emergency resources and their current operational state.
 */
public class ResourceListServlet extends HttpServlet {

    private final ResourceService resourceService;

    public ResourceListServlet(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        ResourceStatus status = parseStatus(request.getParameter("status"));

        List<EmergencyResource> resources = status == null
                ? resourceService.findAll()
                : resourceService.findByStatus(status);

        request.setAttribute("resources", resources);
        request.setAttribute("selectedStatus",
                status == null ? "" : status.name());

        request.getRequestDispatcher("/WEB-INF/views/resources.jsp")
               .forward(request, response);
    }

    private ResourceStatus parseStatus(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return ResourceStatus.valueOf(raw);

        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}