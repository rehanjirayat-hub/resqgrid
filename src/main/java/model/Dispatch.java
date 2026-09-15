package model;

import java.time.LocalDateTime;

public class Dispatch {

    private long id;
    private Incident incident;
    private EmergencyResource resource;
    private ResponseTeam responseTeam;
    private DispatchStatus status;
    private final LocalDateTime createdAt;

    public Dispatch(long id,
                    Incident incident,
                    EmergencyResource resource,
                    ResponseTeam responseTeam) {
        this.id = id;
        this.incident = incident;
        this.resource = resource;
        this.responseTeam = responseTeam;
        this.status = DispatchStatus.CREATED;
        this.createdAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void transitionTo(DispatchStatus newStatus) {
        if (status == DispatchStatus.CREATED
                && newStatus == DispatchStatus.IN_PROGRESS) {

            status = newStatus;

        } else if (status == DispatchStatus.IN_PROGRESS
                && newStatus == DispatchStatus.COMPLETED) {

            status = newStatus;
        }
    }


}