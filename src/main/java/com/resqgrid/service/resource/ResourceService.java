package com.resqgrid.service.resource;

import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.resource.ResourceType;
import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.exception.ResourceNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResourceService {

    private final Map<Long, EmergencyResource> resources;

    public ResourceService() {
        this.resources = new HashMap<>();
    }

    public EmergencyResource registerResource(
            long id,
            String name,
            ResourceType type,
            Location location,
            Set<Capability> capabilities) {

        if (resources.containsKey(id)) {
            throw new IllegalStateException(
                    "Resource with ID " + id + " already exists");
        }

        EmergencyResource resource = new EmergencyResource(
                id,
                name,
                type,
                location,
                capabilities
        );

        resources.put(id, resource);

        return resource;
    }

    public EmergencyResource findResourceById(long id) {

        EmergencyResource resource = resources.get(id);

        if (resource == null) {
            throw new ResourceNotFoundException(
                    "Resource with ID " + id + " was not found"
            );
        }

        return resource;
    }

    public boolean isResourceAvailable(long resourceId) {

        EmergencyResource resource = findResourceById(resourceId);

        return resource.getStatus() == ResourceStatus.AVAILABLE;
    }

    public void changeResourceStatus(long resourceId, ResourceStatus newStatus) {

        EmergencyResource resource = findResourceById(resourceId);

        resource.changeStatus(newStatus);
    }
}