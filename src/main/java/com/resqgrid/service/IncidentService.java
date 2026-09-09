package com.resqgrid.service;

import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.incident.IncidentSeverity;
import com.resqgrid.domain.location.Location;

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
}