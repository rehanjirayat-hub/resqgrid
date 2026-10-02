package service;

import model.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DistanceCalculatorTest {

    @Test
    void distanceToSameLocationShouldBeZero() {

        Location location = new Location(12.9716, 77.5946);

        assertEquals(0.0, DistanceCalculator.distanceInKm(location, location), 0.0001);
    }

    @Test
    void distanceShouldBeSymmetric() {

        Location bengaluru = new Location(12.9716, 77.5946);
        Location delhi = new Location(28.6139, 77.2090);

        double forward = DistanceCalculator.distanceInKm(bengaluru, delhi);
        double backward = DistanceCalculator.distanceInKm(delhi, bengaluru);

        assertEquals(forward, backward, 0.0001);
    }

    @Test
    void distanceBetweenBengaluruAndDelhiShouldBeApproximatelyCorrect() {

        Location bengaluru = new Location(12.9716, 77.5946);
        Location delhi = new Location(28.6139, 77.2090);

        double distance = DistanceCalculator.distanceInKm(bengaluru, delhi);

        // Great-circle distance is roughly 1740 km.
        assertTrue(distance > 1700 && distance < 1780,
                "Expected roughly 1740 km but was " + distance);
    }

    @Test
    void distanceBetweenMumbaiAndPuneShouldBeApproximatelyCorrect() {

        Location mumbai = new Location(19.0760, 72.8777);
        Location pune = new Location(18.5204, 73.8567);

        double distance = DistanceCalculator.distanceInKm(mumbai, pune);

        // These two cities are about 120 km apart.
        assertTrue(distance > 110 && distance < 135,
                "Expected roughly 120 km but was " + distance);
    }

    @Test
    void nearbyLocationsShouldProduceSmallDistance() {

        Location incident = new Location(12.9716, 77.5946);
        Location nearby = new Location(12.9816, 77.6046);

        double distance = DistanceCalculator.distanceInKm(incident, nearby);

        assertTrue(distance > 0 && distance < 5,
                "Expected a small distance but was " + distance);
    }
}