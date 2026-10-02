package service;

import model.Location;

/**
 * Calculates great-circle distance between two locations.
 *
 * The Haversine formula is used because it needs only the latitude and
 * longitude already stored in the {@code locations} table, so no
 * additional column or geospatial extension is required.
 */
public final class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private DistanceCalculator() {
    }

    /**
     * @return distance in kilometres
     */
    public static double distanceInKm(Location from, Location to) {
        double fromLatitudeRadians = Math.toRadians(from.getLatitude());
        double toLatitudeRadians = Math.toRadians(to.getLatitude());

        double latitudeDelta = toLatitudeRadians - fromLatitudeRadians;
        double longitudeDelta =
                Math.toRadians(to.getLongitude() - from.getLongitude());

        double haversine =
                Math.pow(Math.sin(latitudeDelta / 2), 2)
                        + Math.cos(fromLatitudeRadians)
                        * Math.cos(toLatitudeRadians)
                        * Math.pow(Math.sin(longitudeDelta / 2), 2);

        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(haversine));
    }
}