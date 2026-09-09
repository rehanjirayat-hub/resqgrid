package com.resqgrid.service.dispatch;

import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.ResourceType;

public record ResourceRequirement(
        ResourceType resourceType,
        Capability capability
) {

    public ResourceRequirement {
        if (resourceType == null) {
            throw new IllegalArgumentException(
                    "Resource type cannot be null"
            );
        }

        if (capability == null) {
            throw new IllegalArgumentException(
                    "Capability cannot be null"
            );
        }
    }
}