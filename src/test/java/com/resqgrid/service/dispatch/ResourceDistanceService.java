package com.resqgrid.service.dispatch;

import com.resqgrid.domain.location.Location;

public class ResourceDistanceService {

    private static final double EARTH_RADIUS_KILOMETERS = 6371.0;

    public double calculateDistance(Location first, Location second) {

        if (first == null || second == null) {
            throw new IllegalArgumentException(
                    "Locations cannot be null"
            );
        }

        double firstLatitude = Math.toRadians(first.getLatitude());
        double secondLatitude = Math.toRadians(second.getLatitude());

        double latitudeDifference =
                Math.toRadians(
                        second.getLatitude() - first.getLatitude()
                );

        double longitudeDifference =
                Math.toRadians(
                        second.getLongitude() - first.getLongitude()
                );

        double a =
                Math.sin(latitudeDifference / 2)
                        * Math.sin(latitudeDifference / 2)
                        + Math.cos(firstLatitude)
                        * Math.cos(secondLatitude)
                        * Math.sin(longitudeDifference / 2)
                        * Math.sin(longitudeDifference / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS_KILOMETERS * c;
    }
}