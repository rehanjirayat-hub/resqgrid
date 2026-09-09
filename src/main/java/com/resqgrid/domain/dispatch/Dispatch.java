package com.resqgrid.domain.dispatch;

import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.team.ResponseTeam;

import java.time.LocalDateTime;

public class Dispatch {

    private final long id;
    private final Incident incident;
    private EmergencyResource resource;
    private ResponseTeam responseTeam;
    private DispatchStatus status;
    private LocalDateTime dispatchedAt;

    public Dispatch(long id, Incident incident) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Dispatch ID must be greater than zero"
            );
        }

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Dispatch incident cannot be null"
            );
        }

        this.id = id;
        this.incident = incident;
        this.status = DispatchStatus.PENDING;
    }

    public long getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public EmergencyResource getResource() {
        return resource;
    }

    public ResponseTeam getResponseTeam() {
        return responseTeam;
    }

    public DispatchStatus getStatus() {
        return status;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public void assign(
            EmergencyResource resource,
            ResponseTeam responseTeam
    ) {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "Emergency resource cannot be null"
            );
        }

        if (responseTeam == null) {
            throw new IllegalArgumentException(
                    "Response team cannot be null"
            );
        }

        if (status != DispatchStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a pending dispatch can be assigned"
            );
        }

        this.resource = resource;
        this.responseTeam = responseTeam;
        this.status = DispatchStatus.ASSIGNED;
        this.dispatchedAt = LocalDateTime.now();
    }

    public void changeStatus(DispatchStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "New dispatch status cannot be null"
            );
        }

        boolean validTransition =
                (status == DispatchStatus.PENDING
                        && newStatus == DispatchStatus.CANCELLED)

                        || (status == DispatchStatus.ASSIGNED
                        && (newStatus == DispatchStatus.IN_PROGRESS
                        || newStatus == DispatchStatus.CANCELLED))

                        || (status == DispatchStatus.IN_PROGRESS
                        && (newStatus == DispatchStatus.COMPLETED
                        || newStatus == DispatchStatus.CANCELLED));

        if (!validTransition) {
            throw new IllegalStateException(
                    "Invalid dispatch status transition: "
                            + status + " -> " + newStatus
            );
        }

        this.status = newStatus;
    }

    @Override
    public String toString() {
        return "Dispatch{" +
                "id=" + id +
                ", incident=" + incident +
                ", resource=" + resource +
                ", responseTeam=" + responseTeam +
                ", status=" + status +
                ", dispatchedAt=" + dispatchedAt +
                '}';
    }
}