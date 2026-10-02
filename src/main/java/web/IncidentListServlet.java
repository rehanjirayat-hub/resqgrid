package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Incident;
import model.Status;
import service.IncidentService;

import java.io.IOException;
import java.util.List;

/**
 * Displays the current incidents.
 *
 * The controller only prepares model information for the view. Incident rules
 * and persistence remain in the application and data-access layers.
 */
public class IncidentListServlet extends HttpServlet {

    private final IncidentService incidentService;

    public IncidentListServlet(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Status status = parseStatus(request.getParameter("status"));

        List<Incident> incidents = status == null
                ? incidentService.findAll()
                : incidentService.findByStatus(status);

        request.setAttribute("incidents", incidents);
        request.setAttribute("selectedStatus",
                status == null ? "" : status.name());

        request.getRequestDispatcher("/WEB-INF/views/incidents.jsp")
               .forward(request, response);
    }

    private Status parseStatus(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Status.valueOf(raw);

        } catch (IllegalArgumentException e) {
            // An unknown filter simply shows every incident rather than
            // failing the page.
            return null;
        }
    }
}