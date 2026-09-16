package repository;

import model.Location;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocationRepositoryTest {

    @Test
    void saveShouldInsertLocationAndReturnGeneratedId() {

        double latitude = 12.9716;
        double longitude = 77.5946;

        Location location = new Location(latitude, longitude);

        LocationRepository repository = new LocationRepository();

        long id = repository.save(location);

        assertTrue(id > 0);
    }

    @Test
    void findByIdShouldReturnSavedLocation() {

        double latitude = 12.9716;
        double longitude = 77.5946;

        Location location = new Location(latitude, longitude);

        LocationRepository repository = new LocationRepository();

        long id = repository.save(location);

        Optional<Location> result = repository.findById(id);

        assertTrue(result.isPresent());

        Location savedLocation = result.get();

        assertEquals(12.9716, savedLocation.getLatitude());
        assertEquals(77.5946, savedLocation.getLongitude());
    }
}