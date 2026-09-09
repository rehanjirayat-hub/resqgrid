package com.resqgrid.domain.incident;

import com.resqgrid.domain.location.Location;

import java.time.LocalDateTime;

import com.resqgrid.domain.incident.IncidentType;

public class Incident {

    private final long id;
    private final String title;
    private final String description;
    private final IncidentType type;
    private final IncidentSeverity severity;
    private IncidentStatus status;
    private final Location location;
    private final LocalDateTime reportedAt;

    public Incident(
            long id,
            String title,
            String description,
            IncidentType type,
            IncidentSeverity severity,
            Location location,
            LocalDateTime reportedAt) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Incident ID must be greater than zero"
            );
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Incident title cannot be blank"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Incident description cannot be blank"
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Incident type cannot be null"
            );
        }

        if (severity == null) {
            throw new IllegalArgumentException(
                    "Incident severity cannot be null"
            );
        }

        if (location == null) {
            throw new IllegalArgumentException(
                    "Incident location cannot be null"
            );
        }

        if (reportedAt == null) {
            throw new IllegalArgumentException(
                    "Reported time cannot be null"
            );
        }

        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.severity = severity;
        this.status = IncidentStatus.REPORTED;
        this.location = location;
        this.reportedAt = reportedAt;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IncidentType getType() {
        return type;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Location getLocation() {
        return location;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void changeStatus(IncidentStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "New incident status cannot be null"
            );
        }

        if (status == IncidentStatus.RESOLVED) {
            throw new IllegalStateException(
                    "A resolved incident cannot change its status"
            );
        }

        if (status == IncidentStatus.CANCELLED) {
            throw new IllegalStateException(
                    "A cancelled incident cannot change its status"
            );
        }

        boolean validTransition =
                (status == IncidentStatus.REPORTED
                        && newStatus == IncidentStatus.ASSESSED)

                        || (status == IncidentStatus.ASSESSED
                        && newStatus == IncidentStatus.DISPATCHED)

                        || (status == IncidentStatus.DISPATCHED
                        && newStatus == IncidentStatus.IN_PROGRESS)

                        || (status == IncidentStatus.IN_PROGRESS
                        && newStatus == IncidentStatus.RESOLVED)

                        || newStatus == IncidentStatus.CANCELLED;

        if (!validTransition) {
            throw new IllegalStateException(
                    "Invalid incident status transition: "
                            + status + " -> " + newStatus
            );
        }

        this.status = newStatus;
    }
}