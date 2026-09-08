package com.resqgrid.domain.incident;


import java.time.LocalDateTime;

public class Incident {

    private final long id;
    private final String title;
    private final String description;
    private final IncidentSeverity severity;
    private IncidentStatus status;
    private final String location;
    private final LocalDateTime reportedAt;

    public Incident(
            long id,
            String title,
            String description,
            IncidentSeverity severity,
            String location,
            LocalDateTime reportedAt
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("Incident ID must be greater than zero");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Incident title cannot be blank");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Incident description cannot be blank");
        }

        if (severity == null) {
            throw new IllegalArgumentException("Incident severity cannot be null");
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Incident location cannot be blank");
        }

        if (reportedAt == null) {
            throw new IllegalArgumentException("Reported time cannot be null");
        }

        this.id = id;
        this.title = title;
        this.description = description;
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

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void changeStatus(IncidentStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException("New incident status cannot be null");
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
                status == IncidentStatus.REPORTED && newStatus == IncidentStatus.ASSESSED || status == IncidentStatus.ASSESSED && newStatus == IncidentStatus.DISPATCHED || status == IncidentStatus.DISPATCHED && newStatus == IncidentStatus.IN_PROGRESS || status == IncidentStatus.IN_PROGRESS && newStatus == IncidentStatus.RESOLVED || newStatus == IncidentStatus.CANCELLED;

        if (!validTransition) {
            throw new IllegalStateException(
                    "Invalid incident status transition: "
                            + status + " -> " + newStatus
            );
        }

        this.status = newStatus;
    }

    @Override
    public String toString() {
        return "Incident{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", severity=" + severity +
                ", status=" + status +
                ", location='" + location + '\'' +
                ", reportedAt=" + reportedAt +
                '}';
    }
}
