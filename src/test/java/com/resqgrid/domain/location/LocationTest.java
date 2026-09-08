package com.resqgrid.domain.location;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocationTest {

    @Nested
    class locationTest {

        @Test
        void validLocationShouldBeCreated() {
            Location location = new Location(
                    12.2958,
                    76.6394,
                    "Mysuru, Karnataka"
            );

            assertEquals(12.2958, location.getLatitude());
            assertEquals(76.6394, location.getLongitude());
            assertEquals("Mysuru, Karnataka", location.getAddress());
        }

        @Test
        void latitudeBelowMinus90ShouldBeRejected() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Location(
                            -90.1,
                            76.6394,
                            "Mysuru, Karnataka"
                    )
            );
        }

        @Test
        void latitudeAbove90ShouldBeRejected() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Location(
                            90.1,
                            76.6394,
                            "Mysuru, Karnataka"
                    )
            );
        }

        @Test
        void longitudeBelowMinus180ShouldBeRejected() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Location(
                            12.2958,
                            -180.1,
                            "Mysuru, Karnataka"
                    )
            );
        }

        @Test
        void longitudeAbove180ShouldBeRejected() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Location(
                            12.2958,
                            180.1,
                            "Mysuru, Karnataka"
                    )
            );
        }

        @Test
        void blankAddressShouldBeRejected() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Location(
                            12.2958,
                            76.6394,
                            ""
                    )
            );
        }

        @Test
        void nullAddressShouldBeRejected() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Location(
                            12.2958,
                            76.6394,
                            null
                    )
            );
        }
    }
}
