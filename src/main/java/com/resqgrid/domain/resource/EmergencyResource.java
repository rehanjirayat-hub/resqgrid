package com.resqgrid.domain.resource;

import com.resqgrid.domain.location.Location;

import java.util.HashSet;
import java.util.Set;

public class EmergencyResource {

    private final long id;
    private final String name;
    private ResourceType type;
    private ResourceStatus status;
    private Location location;
    private final Set<Capability> capabilities;

    public EmergencyResource(
            long id,
            String name,
            ResourceType type,
            Location location,
            Set<Capability> capabilities
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Resource ID must be greater than zero"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Resource name cannot be blank"
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Resource type cannot be null"
            );
        }

        if (location == null) {
            throw new IllegalArgumentException(
                    "Resource location cannot be null"
            );
        }

        if (capabilities == null || capabilities.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resource must have at least one capability"
            );
        }

        this.id = id;
        this.name = name;
        this.type = type;
        this.status = ResourceStatus.AVAILABLE;
        this.location = location;
        this.capabilities = new HashSet<>(capabilities);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public Location getLocation() {
        return location;
    }

    public Set<Capability> getCapabilities() {
        return Set.copyOf(capabilities);
    }

    @Override
    public String toString() {
        return "EmergencyResource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", location=" + location +
                ", capabilities=" + capabilities +
                '}';
    }

    public void changeStatus(ResourceStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "New resource status cannot be null"
            );
        }

        boolean validTransition =
                (status == ResourceStatus.AVAILABLE
                        && (newStatus == ResourceStatus.BUSY
                        || newStatus == ResourceStatus.OFFLINE
                        || newStatus == ResourceStatus.MAINTENANCE))

                        || (status == ResourceStatus.BUSY
                        && (newStatus == ResourceStatus.AVAILABLE
                        || newStatus == ResourceStatus.OFFLINE))

                        || (status == ResourceStatus.OFFLINE
                        && (newStatus == ResourceStatus.AVAILABLE
                        || newStatus == ResourceStatus.MAINTENANCE))

                        || (status == ResourceStatus.MAINTENANCE
                        && (newStatus == ResourceStatus.AVAILABLE
                        || newStatus == ResourceStatus.OFFLINE));

        if (!validTransition) {
            throw new IllegalStateException(
                    "Invalid resource status transition: "
                            + status + " -> " + newStatus
            );
        }

        this.status = newStatus;
    }
}