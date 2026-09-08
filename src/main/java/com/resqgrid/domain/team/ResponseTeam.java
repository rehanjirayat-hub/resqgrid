package com.resqgrid.domain.team;

import com.resqgrid.domain.resource.Capability;

import java.util.HashSet;
import java.util.Set;

public class ResponseTeam {

    private final long id;
    private final String name;
    private TeamStatus status;
    private final Set<Capability> capabilities;

    public ResponseTeam(
            long id,
            String name,
            Set<Capability> capabilities
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Team ID must be greater than zero"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Team name cannot be blank"
            );
        }

        if (capabilities == null || capabilities.isEmpty()) {
            throw new IllegalArgumentException(
                    "Team must have at least one capability"
            );
        }

        this.id = id;
        this.name = name;
        this.status = TeamStatus.AVAILABLE;
        this.capabilities = new HashSet<>(capabilities);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TeamStatus getStatus() {
        return status;
    }

    public Set<Capability> getCapabilities() {
        return Set.copyOf(capabilities);
    }

    public void changeStatus(TeamStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "New team status cannot be null"
            );
        }

        boolean validTransition =
                (status == TeamStatus.AVAILABLE
                        && (newStatus == TeamStatus.DEPLOYED
                        || newStatus == TeamStatus.OFFLINE))

                        || (status == TeamStatus.DEPLOYED
                        && (newStatus == TeamStatus.AVAILABLE
                        || newStatus == TeamStatus.OFFLINE))

                        || (status == TeamStatus.OFFLINE
                        && newStatus == TeamStatus.AVAILABLE);

        if (!validTransition) {
            throw new IllegalStateException(
                    "Invalid team status transition: "
                            + status + " -> " + newStatus
            );
        }

        this.status = newStatus;
    }

    @Override
    public String toString() {
        return "ResponseTeam{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", capabilities=" + capabilities +
                '}';
    }
}