package com.resqgrid.service.dispatch;

import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.EmergencyResource;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ResourceRankingService {

    private final ResourceDistanceService distanceService;

    public ResourceRankingService() {
        this.distanceService = new ResourceDistanceService();
    }

    public List<EmergencyResource> rankResources(
            Collection<EmergencyResource> resources,
            Location incidentLocation) {

        if (resources == null) {
            throw new IllegalArgumentException(
                    "Resources cannot be null"
            );
        }

        if (incidentLocation == null) {
            throw new IllegalArgumentException(
                    "Incident location cannot be null"
            );
        }

        Comparator<EmergencyResource> byDistance =
                Comparator.comparingDouble(
                        resource -> distanceService.calculateDistance(
                                resource.getLocation(),
                                incidentLocation
                        )
                );

        Comparator<EmergencyResource> byDistanceThenId =
                byDistance.thenComparingLong(
                        EmergencyResource::getId
                );

        return resources.stream()
                .sorted(byDistanceThenId)
                .toList();
    }
}