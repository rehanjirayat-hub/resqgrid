package com.resqgrid.service.dispatch;

import com.resqgrid.domain.location.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDistanceServiceTest {

    private ResourceDistanceService distanceService;

    @BeforeEach
    void setUp() {
        distanceService = new ResourceDistanceService();
    }

    @Test
    void shouldReturnZeroWhenLocationsAreTheSame() {

        Location location = new Location(
                12.9716,
                77.5946,
                "Bengaluru"
        );

        double distance =
                distanceService.calculateDistance(
                        location,
                        location
                );

        assertEquals(0.0, distance, 0.0001);
    }

    @Test
    void shouldCalculateDistanceBetweenTwoLocations() {

        Location bengaluru = new Location(
                12.9716,
                77.5946,
                "Bengaluru"
        );

        Location mysuru = new Location(
                12.2958,
                76.6394,
                "Mysuru"
        );

        double distance =
                distanceService.calculateDistance(
                        bengaluru,
                        mysuru
                );

        assertTrue(distance > 0);
    }

    @Test
    void shouldCalculateDistanceSymmetrically() {

        Location bengaluru = new Location(
                12.9716,
                77.5946,
                "Bengaluru"
        );

        Location mysuru = new Location(
                12.2958,
                76.6394,
                "Mysuru"
        );

        double firstDistance =
                distanceService.calculateDistance(
                        bengaluru,
                        mysuru
                );

        double secondDistance =
                distanceService.calculateDistance(
                        mysuru,
                        bengaluru
                );

        assertEquals(
                firstDistance,
                secondDistance,
                0.0001
        );
    }

    @Test
    void shouldRejectNullFirstLocation() {

        Location location = new Location(
                12.9716,
                77.5946,
                "Bengaluru"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> distanceService.calculateDistance(
                        null,
                        location
                )
        );
    }

    @Test
    void shouldRejectNullSecondLocation() {

        Location location = new Location(
                12.9716,
                77.5946,
                "Bengaluru"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> distanceService.calculateDistance(
                        location,
                        null
                )
        );
    }
}