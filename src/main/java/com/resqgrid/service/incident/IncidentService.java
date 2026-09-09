package com.resqgrid.service.incident;

import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.incident.IncidentSeverity;
import com.resqgrid.domain.incident.IncidentStatus;
import com.resqgrid.domain.location.Location;
import com.resqgrid.exception.IncidentNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class IncidentService {

    private final Map<Long, Incident> incidents;

    public IncidentService() {
        this.incidents = new HashMap<>();
    }

    public Incident reportIncident(
            long id,
            String title,
            String description,
            IncidentSeverity severity,
            Location location,
            LocalDateTime reportedAt
    ) {
        if (incidents.containsKey(id)) {
            throw new IllegalStateException(
                    "Incident with ID " + id + " already exists"
            );
        }

        Incident incident = new Incident(
                id,
                title,
                description,
                severity,
                location,
                reportedAt
        );

        incidents.put(id, incident);

        return incident;
    }

    public Incident findIncidentById(long id) {
        Incident incident = incidents.get(id);

        if (incident == null) {
            throw new IncidentNotFoundException(
                    "Incident with ID " + id + " was not found"
            );
        }

        return incident;
    }

    public void changeIncidentStatus(
            long incidentId,
            IncidentStatus newStatus
    ) {
        Incident incident = findIncidentById(incidentId);

        incident.changeStatus(newStatus);
    }

    public void cancelIncident(long incidentId) {
        Incident incident = findIncidentById(incidentId);
        incident.changeStatus(IncidentStatus.CANCELLED);
    }
}