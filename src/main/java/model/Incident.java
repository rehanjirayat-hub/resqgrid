package model;

import java.time.LocalDateTime;

public class Incident {
    private long id;
    private String description;
    private Severity severity;
    private Status status;
    private Location location;
    private final LocalDateTime createdAt;

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Status getStatus() {
        return status;
    }

    public Location getLocation() {
        return location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Incident(String description, Severity severity, Location location) {
        this.description = description;
        this.severity = severity;
        this.location = location;
        this.status = Status.REPORTED;
        this.createdAt = LocalDateTime.now();
    }


    public void transitionTo(Status newStatus) {
        if (status == Status.REPORTED && newStatus == Status.ASSESSED) {
            status = newStatus;
        } else if (status == Status.ASSESSED && newStatus == Status.REQUIREMENTS_DETERMINED) {
            status = newStatus;
        } else if (status == Status.REQUIREMENTS_DETERMINED && newStatus == Status.RESPONSE_IN_PROGRESS) {
            status = newStatus;
        } else if (status == Status.RESPONSE_IN_PROGRESS && newStatus == Status.RESOLVED) {
            status = newStatus;
        }
    }
}


