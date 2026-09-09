package com.resqgrid.service.dispatch;

import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.incident.IncidentType;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.ResourceType;

public class IncidentResourceRequirementService {

    public ResourceRequirement determineRequirement(
            Incident incident) {

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Incident cannot be null"
            );
        }

        return switch (incident.getType()) {

            case MEDICAL ->
                    new ResourceRequirement(
                            ResourceType.AMBULANCE,
                            Capability.MEDICAL_RESPONSE
                    );

            case FIRE ->
                    new ResourceRequirement(
                            ResourceType.FIRE_UNIT,
                            Capability.FIRE_RESPONSE
                    );

            case RESCUE ->
                    new ResourceRequirement(
                            ResourceType.RESCUE_TEAM,
                            Capability.RESCUE
                    );

            case WATER_RESCUE ->
                    new ResourceRequirement(
                            ResourceType.RESCUE_TEAM,
                            Capability.WATER_RESCUE
                    );

            case HAZARDOUS_MATERIAL ->
                    new ResourceRequirement(
                            ResourceType.FIRE_UNIT,
                            Capability.HAZARDOUS_MATERIALS
                    );
        };
    }
}