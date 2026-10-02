package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Dispatch;
import service.DispatchService;

import java.io.IOException;
import java.util.List;

/**
 * Dispatches for an incident or for a resource, including their lifecycle
 * state.
 *
 * The controller selects the appropriate application-service call and hands
 * the result to the view. It performs no dispatch decisions of its own.
 */
public class DispatchListServlet extends HttpServlet {

    private final DispatchService dispatchService;

    public DispatchListServlet(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("dispatches", loadDispatches(request));

        request.getRequestDispatcher("/WEB-INF/views/dispatches.jsp")
               .forward(request, response);
    }

    private List<Dispatch> loadDispatches(HttpServletRequest request) {

        Long incidentId = parseId(request.getParameter("incidentId"));
        Long resourceId = parseId(request.getParameter("resourceId"));

        if (incidentId != null) {
            return dispatchService.findByIncident(incidentId);
        }

        if (resourceId != null) {
            return dispatchService.findByResource(resourceId);
        }

        return dispatchService.findAll();
    }

    /**
     * An absent or malformed identifier means "no filter" rather than an
     * error, so the page still renders.
     */
    private Long parseId(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(raw.trim());

        } catch (NumberFormatException e) {
            return null;
        }
    }
}